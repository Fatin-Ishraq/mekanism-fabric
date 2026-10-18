package mekanism.neoshim.neoforge.fluids.crafting;

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

    public record Type<T extends FluidIngredient>(Identifier id, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
    }

    private static final Map<Identifier, Type<?>> TYPES = new LinkedHashMap<>();

    public static synchronized <T extends FluidIngredient> Type<T> register(Identifier id, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        Type<T> type = new Type<>(id, codec, streamCodec);
        if (TYPES.putIfAbsent(id, type) != null) {
            throw new IllegalStateException("Duplicate fluid ingredient type " + id);
        }
        return type;
    }

    /** Dispatches on "type"; without a type, falls back to single ({@code fluid}) / tag ({@code tag}). */
    public static final MapCodec<FluidIngredient> MAP_CODEC_NONEMPTY = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(ops.createString("type"));
        }

        @Override
        public <T> DataResult<FluidIngredient> decode(DynamicOps<T> ops, MapLike<T> input) {
            bootstrap();
            T typeValue = input.get("type");
            if (typeValue == null) {
                //Converted resources use Fabric's key (see build.gradle D11 transform)
                typeValue = input.get("fabric:type");
            }
            if (typeValue == null) {
                if (input.get("fluid") != null) {
                    return SingleFluidIngredient.CODEC.decode(ops, input).map(i -> i);
                } else if (input.get("tag") != null) {
                    return TagFluidIngredient.CODEC.decode(ops, input).map(i -> i);
                }
                return DataResult.error(() -> "Fluid ingredient needs a 'type', 'fluid' or 'tag'");
            }
            return Identifier.CODEC.parse(ops, typeValue).flatMap(id -> {
                Type<?> type = TYPES.get(id);
                if (type == null) {
                    return DataResult.error(() -> "Unknown fluid ingredient type " + id);
                }
                return type.codec().decode(ops, input).map(i -> (FluidIngredient) i);
            });
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T> RecordBuilder<T> encode(FluidIngredient input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            Type type = input.getType();
            if (input instanceof SingleFluidIngredient || input instanceof TagFluidIngredient) {
                return type.codec().encode(input, ops, prefix);
            }
            prefix.add("type", ops.createString(type.id().toString()));
            return type.codec().encode(input, ops, prefix);
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public FluidIngredient decode(RegistryFriendlyByteBuf buf) {
            bootstrap();
            Identifier id = Identifier.STREAM_CODEC.decode(buf);
            Type<?> type = TYPES.get(id);
            if (type == null) {
                throw new io.netty.handler.codec.DecoderException("Unknown fluid ingredient type " + id);
            }
            return type.streamCodec().decode(buf);
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void encode(RegistryFriendlyByteBuf buf, FluidIngredient ingredient) {
            Type type = ingredient.getType();
            Identifier.STREAM_CODEC.encode(buf, type.id());
            type.streamCodec().encode(buf, ingredient);
        }
    };

    /** Ensures the built-in types are registered before any lookup by id. */
    private static void bootstrap() {
        Object[] unused = {EmptyFluidIngredient.TYPE, SingleFluidIngredient.TYPE, TagFluidIngredient.TYPE, CompoundFluidIngredient.TYPE,
                           DataComponentFluidIngredient.TYPE};
    }

    private FluidIngredientTypes() {
    }
}
