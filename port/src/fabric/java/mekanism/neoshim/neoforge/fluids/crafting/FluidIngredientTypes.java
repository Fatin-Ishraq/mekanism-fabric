package mekanism.neoshim.neoforge.fluids.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/** Registry of fluid ingredient types (NeoForge's FLUID_INGREDIENT_TYPES), keyed by the same ids NeoForge uses. */
public final class FluidIngredientTypes {

    private static final Map<Identifier, FluidIngredientType<?>> TYPES = new LinkedHashMap<>();

    public static synchronized <T extends FluidIngredient> FluidIngredientType<T> register(Identifier id, MapCodec<T> codec,
          StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        FluidIngredientType<T> type = new FluidIngredientType<>(codec, streamCodec);
        if (TYPES.putIfAbsent(id, type) != null) {
            throw new IllegalStateException("Duplicate fluid ingredient type " + id);
        }
        return type;
    }

    /** Dispatches on {@code neoforge:ingredient_type} ({@code fabric:type} after the build's data rewrite, {@code type} in older data). */
    private static final MapCodec<FluidIngredient> DISPATCH_CODEC = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(ops.createString("neoforge:ingredient_type"), ops.createString("type"));
        }

        @Override
        public <T> DataResult<FluidIngredient> decode(DynamicOps<T> ops, MapLike<T> input) {
            bootstrap();
            T typeValue = input.get("neoforge:ingredient_type");
            if (typeValue == null) {
                typeValue = input.get("fabric:type");
            }
            if (typeValue == null) {
                typeValue = input.get("type");
            }
            if (typeValue == null) {
                return DataResult.error(() -> "Fluid ingredient needs a 'neoforge:ingredient_type'");
            }
            T finalTypeValue = typeValue;
            return Identifier.CODEC.parse(ops, finalTypeValue).flatMap(id -> {
                FluidIngredientType<?> type = TYPES.get(id);
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
            Identifier id = idFor(type);
            prefix.add("neoforge:ingredient_type", ops.createString(id.toString()));
            return type.codec().encode(input, ops, prefix);
        }
    };

    static Identifier idFor(FluidIngredientType<?> type) {
        bootstrap();
        for (Map.Entry<Identifier, FluidIngredientType<?>> entry : TYPES.entrySet()) {
            if (entry.getValue() == type) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Unregistered fluid ingredient type " + type);
    }

    public static final Codec<FluidIngredient> CODEC = Codec.lazyInitialized(() -> Codec.either(SimpleFluidIngredient.CODEC, DISPATCH_CODEC.codec())
          .xmap(either -> either.map(simple -> simple, custom -> custom),
                ingredient -> ingredient instanceof SimpleFluidIngredient simple ? Either.left(simple) : Either.right(ingredient)));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public FluidIngredient decode(RegistryFriendlyByteBuf buf) {
            bootstrap();
            Identifier id = Identifier.STREAM_CODEC.decode(buf);
            FluidIngredientType<?> type = TYPES.get(id);
            if (type == null) {
                throw new io.netty.handler.codec.DecoderException("Unknown fluid ingredient type " + id);
            }
            return type.streamCodec().decode(buf);
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void encode(RegistryFriendlyByteBuf buf, FluidIngredient ingredient) {
            FluidIngredientType type = ingredient.getType();
            Identifier.STREAM_CODEC.encode(buf, idFor(type));
            type.streamCodec().encode(buf, ingredient);
        }
    };

    /** Ensures the built-in types are registered before any lookup by id. */
    private static void bootstrap() {
        Object[] unused = {SimpleFluidIngredient.TYPE, CompoundFluidIngredient.TYPE, DataComponentFluidIngredient.TYPE};
    }

    private FluidIngredientTypes() {
    }
}
