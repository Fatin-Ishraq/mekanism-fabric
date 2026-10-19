package mekanism.neoshim.neoforge.fluids.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import io.netty.handler.codec.DecoderException;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.fluids.crafting.display.FluidSlotDisplay;
import mekanism.neoshim.neoforge.fluids.crafting.display.FluidStackSlotDisplay;
import mekanism.neoshim.neoforge.fluids.crafting.display.FluidTagSlotDisplay;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Fluid ingredient serialization. JSON: a fluid holder set ({@code "minecraft:water"}, {@code "#c:water"}, or a list) for {@link SimpleFluidIngredient},
 * otherwise a map dispatched on {@code neoforge:ingredient_type} (the resource transform may rename that key to {@code fabric:type}; both are read).
 */
public class FluidIngredientCodecs {

    static final String TYPE_KEY = "neoforge:ingredient_type";
    private static final String FABRIC_TYPE_KEY = "fabric:type";

    static final Codec<HolderSet<Fluid>> HOLDER_SET_CODEC = ExtraCodecs.nonEmptyHolderSet(HolderSetCodec.create(Registries.FLUID,
          BuiltInRegistries.FLUID.holderByNameCodec().validate(fluid -> fluid.is(Fluids.EMPTY.builtInRegistryHolder())
                                                                        ? DataResult.error(() -> "Fluid must not be minecraft:empty") : DataResult.success(fluid)),
          false));

    /** Built-in types, initialized on first use so the ingredient classes can reference {@link FluidIngredient#CODEC} in their own codecs. */
    static final class Types {

        static final FluidIngredientType<SimpleFluidIngredient> SIMPLE = new FluidIngredientType<>(
              HOLDER_SET_CODEC.fieldOf("fluids").xmap(SimpleFluidIngredient::new, SimpleFluidIngredient::fluidSet),
              ByteBufCodecs.holderSet(Registries.FLUID).map(SimpleFluidIngredient::new, SimpleFluidIngredient::fluidSet));
        static final FluidIngredientType<CompoundFluidIngredient> COMPOUND = new FluidIngredientType<>(CompoundFluidIngredient.CODEC, CompoundFluidIngredient.STREAM_CODEC);
        static final FluidIngredientType<DataComponentFluidIngredient> DATA_COMPONENT = new FluidIngredientType<>(DataComponentFluidIngredient.CODEC,
              DataComponentFluidIngredient.STREAM_CODEC);
        static final FluidIngredientType<DifferenceFluidIngredient> DIFFERENCE = new FluidIngredientType<>(DifferenceFluidIngredient.CODEC,
              DifferenceFluidIngredient.STREAM_CODEC);
        static final FluidIngredientType<IntersectionFluidIngredient> INTERSECTION = new FluidIngredientType<>(IntersectionFluidIngredient.CODEC,
              IntersectionFluidIngredient.STREAM_CODEC);
        static final FluidIngredientType<CustomDisplayFluidIngredient> CUSTOM_DISPLAY = new FluidIngredientType<>(CustomDisplayFluidIngredient.CODEC,
              CustomDisplayFluidIngredient.STREAM_CODEC);

        private static final Map<Identifier, FluidIngredientType<?>> BY_ID = new LinkedHashMap<>();
        private static final Map<FluidIngredientType<?>, Identifier> IDS = new IdentityHashMap<>();

        static {
            register("simple", SIMPLE);
            register("compound", COMPOUND);
            register("components", DATA_COMPONENT);
            register("difference", DIFFERENCE);
            register("intersection", INTERSECTION);
            register("custom_display", CUSTOM_DISPLAY);
        }

        private static void register(String path, FluidIngredientType<?> type) {
            register(Identifier.fromNamespaceAndPath("neoforge", path), type);
        }

        static synchronized void register(Identifier id, FluidIngredientType<?> type) {
            if (BY_ID.putIfAbsent(id, type) != null || IDS.putIfAbsent(type, id) != null) {
                throw new IllegalStateException("Duplicate fluid ingredient type " + id);
            }
        }
    }

    public static FluidIngredientType<SimpleFluidIngredient> simpleType() {
        return Types.SIMPLE;
    }

    /** Registers a fluid ingredient type of another mod (NeoForge's FLUID_INGREDIENT_TYPES registry). */
    public static void registerType(Identifier id, FluidIngredientType<?> type) {
        Types.register(id, type);
    }

    static Identifier idOf(FluidIngredientType<?> type) {
        Identifier id = Types.IDS.get(type);
        if (id == null) {
            throw new IllegalStateException("Unregistered fluid ingredient type " + type);
        }
        return id;
    }

    private static final MapCodec<FluidIngredient> DISPATCH_CODEC = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(ops.createString(TYPE_KEY));
        }

        @Override
        public <T> DataResult<FluidIngredient> decode(DynamicOps<T> ops, MapLike<T> input) {
            T typeValue = input.get(TYPE_KEY);
            if (typeValue == null) {
                typeValue = input.get(FABRIC_TYPE_KEY);
            }
            if (typeValue == null) {
                return DataResult.error(() -> "Fluid ingredient needs a '" + TYPE_KEY + "'");
            }
            return Identifier.CODEC.parse(ops, typeValue).flatMap(id -> {
                FluidIngredientType<?> type = Types.BY_ID.get(id);
                if (type == null) {
                    return DataResult.error(() -> "Unknown fluid ingredient type " + id);
                }
                return type.codec().decode(ops, input).map(ingredient -> (FluidIngredient) ingredient);
            });
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T> RecordBuilder<T> encode(FluidIngredient input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            FluidIngredientType type = input.getType();
            prefix.add(TYPE_KEY, ops.createString(idOf(type).toString()));
            return type.codec().encode(input, ops, prefix);
        }
    };

    static final Codec<FluidIngredient> CODEC = Codec.either(HOLDER_SET_CODEC, DISPATCH_CODEC.codec()).xmap(
          either -> either.map(SimpleFluidIngredient::new, ingredient -> ingredient),
          ingredient -> ingredient instanceof SimpleFluidIngredient simple ? Either.left(simple.fluidSet()) : Either.right(ingredient));

    static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public FluidIngredient decode(RegistryFriendlyByteBuf buf) {
            Identifier id = Identifier.STREAM_CODEC.decode(buf);
            FluidIngredientType<?> type = Types.BY_ID.get(id);
            if (type == null) {
                throw new DecoderException("Unknown fluid ingredient type " + id);
            }
            return type.streamCodec().decode(buf);
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void encode(RegistryFriendlyByteBuf buf, FluidIngredient ingredient) {
            FluidIngredientType type = ingredient.getType();
            Identifier.STREAM_CODEC.encode(buf, idOf(type));
            ((StreamCodec) type.streamCodec()).encode(buf, ingredient);
        }
    };

    /** Lazily resolves {@link FluidIngredient#STREAM_CODEC}, for codecs of ingredients that contain other fluid ingredients. */
    static StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> lazyStreamCodec() {
        return new StreamCodec<>() {
            @Override
            public FluidIngredient decode(RegistryFriendlyByteBuf buf) {
                return FluidIngredient.STREAM_CODEC.decode(buf);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, FluidIngredient ingredient) {
                FluidIngredient.STREAM_CODEC.encode(buf, ingredient);
            }
        };
    }

    /** Registers the fluid slot display types (NeoForge registers them as {@code neoforge:*} slot displays). Called by the bootstrap. */
    public static void registerSlotDisplays() {
        Registry.register(BuiltInRegistries.SLOT_DISPLAY, Identifier.fromNamespaceAndPath("neoforge", "fluid"), FluidSlotDisplay.TYPE);
        Registry.register(BuiltInRegistries.SLOT_DISPLAY, Identifier.fromNamespaceAndPath("neoforge", "fluid_stack"), FluidStackSlotDisplay.TYPE);
        Registry.register(BuiltInRegistries.SLOT_DISPLAY, Identifier.fromNamespaceAndPath("neoforge", "fluid_tag"), FluidTagSlotDisplay.TYPE);
    }
}
