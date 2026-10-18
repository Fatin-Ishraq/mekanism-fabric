package mekanism.fabric.compat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Codec helpers giving vanilla {@link Ingredient}s NeoForge's map-codec shape (needed for "flat" sized ingredients such as
 * {@code {"count": 2, "tag": "c:ingots/osmium"}}). Custom ingredients are dispatched by {@code "type"} (NeoForge format) or
 * {@code "fabric:type"} (Fabric format) through Fabric's {@link CustomIngredientSerializer} registry.
 */
public final class IngredientCodecs {

    public static final MapCodec<Ingredient> MAP_CODEC_NONEMPTY = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(ops.createString("item"), ops.createString("tag"), ops.createString("type"));
        }

        @Override
        public <T> DataResult<Ingredient> decode(DynamicOps<T> ops, MapLike<T> input) {
            T type = input.get("type");
            if (type == null) {
                type = input.get("fabric:type");
            }
            if (type != null) {
                return ResourceLocation.CODEC.parse(ops, type).flatMap(id -> {
                    CustomIngredientSerializer<?> serializer = CustomIngredientSerializer.get(id);
                    if (serializer == null) {
                        return DataResult.error(() -> "Unknown ingredient type " + id);
                    }
                    return serializer.getCodec(false).decode(ops, input).map(CustomIngredient::toVanilla);
                });
            }
            T item = input.get("item");
            if (item != null) {
                return net.minecraft.core.registries.BuiltInRegistries.ITEM.holderByNameCodec().parse(ops, item).map(holder -> Ingredient.of(new ItemStack(holder)));
            }
            T tag = input.get("tag");
            if (tag != null) {
                return TagKey.codec(Registries.ITEM).parse(ops, tag).map(Ingredient::of);
            }
            return DataResult.error(() -> "Ingredient needs an 'item', 'tag' or 'type'");
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T> RecordBuilder<T> encode(Ingredient input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            CustomIngredient custom = input.getCustomIngredient();
            if (custom != null) {
                CustomIngredientSerializer serializer = custom.getSerializer();
                String key = serializer.getIdentifier().getNamespace().equals("neoforge") ? "type" : "fabric:type";
                prefix.add(key, ops.createString(serializer.getIdentifier().toString()));
                return serializer.getCodec(false).encode(custom, ops, prefix);
            }
            //Vanilla: delegate to the vanilla codec and merge its single map value
            return Ingredient.CODEC_NONEMPTY.encodeStart(ops, input).flatMap(ops::getMap).map(map -> {
                map.entries().forEach(pair -> prefix.add(pair.getFirst(), pair.getSecond()));
                return prefix;
            }).result().orElseGet(() -> prefix.withErrorsFrom(DataResult.error(() -> "Cannot encode multi-value ingredient as a map: " + input)));
        }
    };

    public static final Codec<List<Ingredient>> LIST_CODEC_NONEMPTY = Ingredient.CODEC_NONEMPTY.listOf()
          .validate(list -> list.isEmpty() ? DataResult.error(() -> "Ingredient list cannot be empty") : DataResult.success(list));

    public static <T extends CustomIngredient> CustomIngredientSerializer<T> serializer(ResourceLocation id, MapCodec<T> codec,
          StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return new CustomIngredientSerializer<>() {
            @Override
            public ResourceLocation getIdentifier() {
                return id;
            }

            @Override
            public MapCodec<T> getCodec(boolean allowEmpty) {
                return codec;
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, T> getPacketCodec() {
                return streamCodec;
            }
        };
    }

    /** Registers the NeoForge-format custom ingredient serializers with Fabric. Called by the bootstrap. */
    public static void registerNeoForgeIngredientTypes() {
        CustomIngredientSerializer.register(mekanism.neoshim.neoforge.common.crafting.CompoundIngredient.SERIALIZER);
        CustomIngredientSerializer.register(mekanism.neoshim.neoforge.common.crafting.DifferenceIngredient.SERIALIZER);
        CustomIngredientSerializer.register(mekanism.neoshim.neoforge.common.crafting.IntersectionIngredient.SERIALIZER);
        CustomIngredientSerializer.register(mekanism.neoshim.neoforge.common.crafting.DataComponentIngredient.SERIALIZER);
    }

    private IngredientCodecs() {
    }
}
