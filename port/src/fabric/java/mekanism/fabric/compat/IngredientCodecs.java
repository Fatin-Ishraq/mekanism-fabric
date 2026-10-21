package mekanism.fabric.compat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import mekanism.neoshim.neoforge.common.crafting.CompoundIngredient;
import mekanism.neoshim.neoforge.common.crafting.DataComponentIngredient;
import mekanism.neoshim.neoforge.common.crafting.DifferenceIngredient;
import mekanism.neoshim.neoforge.common.crafting.ICustomIngredient;
import mekanism.neoshim.neoforge.common.crafting.IngredientType;
import mekanism.neoshim.neoforge.common.crafting.IntersectionIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Codec helpers for ingredients. Custom ingredient serializers are registered with Fabric under their NeoForge ids so the Fabric-patched
 * {@link Ingredient#CODEC} dispatches {@code fabric:type}; {@link #MAP_CODEC_NONEMPTY} additionally accepts NeoForge's flat map form.
 */
public final class IngredientCodecs {

    private static final Map<IngredientType<?>, CustomIngredientSerializer<?>> SERIALIZERS = new IdentityHashMap<>();

    public static final MapCodec<Ingredient> MAP_CODEC_NONEMPTY = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(ops.createString("item"), ops.createString("tag"), ops.createString("fabric:type"));
        }

        @Override
        public <T> DataResult<Ingredient> decode(DynamicOps<T> ops, MapLike<T> input) {
            T type = input.get("fabric:type");
            if (type == null) {
                type = input.get("type");
            }
            if (type == null) {
                type = input.get("neoforge:ingredient_type");
            }
            if (type != null) {
                T finalType = type;
                return Identifier.CODEC.parse(ops, finalType).flatMap(id -> {
                    CustomIngredientSerializer<?> serializer = CustomIngredientSerializer.get(id);
                    if (serializer == null) {
                        return DataResult.error(() -> "Unknown ingredient type " + id);
                    }
                    return serializer.getCodec().decode(ops, input).map(CustomIngredient::toVanilla);
                });
            }
            T item = input.get("item");
            if (item != null) {
                return net.minecraft.core.registries.BuiltInRegistries.ITEM.holderByNameCodec().parse(ops, item)
                      .map(holder -> Ingredient.of(HolderSet.direct(holder)));
            }
            T tag = input.get("tag");
            if (tag != null) {
                return TagKey.codec(Registries.ITEM).parse(ops, tag).flatMap(IngredientCodecs::fromTag);
            }
            return DataResult.error(() -> "Ingredient needs an 'item', 'tag' or 'fabric:type'");
        }

        @Override
        public <T> RecordBuilder<T> encode(Ingredient input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            CustomIngredient custom = input.getCustomIngredient();
            if (custom != null) {
                return encodeCustom(custom, ops, prefix);
            }
            return Ingredient.CODEC.encodeStart(ops, input).flatMap(ops::getMap).map(map -> {
                map.entries().forEach(pair -> prefix.add(pair.getFirst(), pair.getSecond()));
                return prefix;
            }).result().orElseGet(() -> prefix.withErrorsFrom(DataResult.error(() -> "Cannot encode ingredient as a map: " + input)));
        }
    };

    /** 26.1's {@code Ingredient} only builds from a {@link HolderSet}, so a decoded tag has to be resolved against the item registry first. */
    private static DataResult<Ingredient> fromTag(TagKey<Item> tag) {
        HolderSet.Named<Item> holders = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(tag).orElse(null);
        return holders == null ? DataResult.error(() -> "Unknown item tag " + tag.location()) : DataResult.success(Ingredient.of(holders));
    }

    @SuppressWarnings("unchecked")
    private static <T> RecordBuilder<T> encodeCustom(CustomIngredient custom, DynamicOps<T> ops, RecordBuilder<T> prefix) {        CustomIngredientSerializer<CustomIngredient> serializer = (CustomIngredientSerializer<CustomIngredient>) custom.getSerializer();
        prefix.add("fabric:type", ops.createString(serializer.getIdentifier().toString()));
        return serializer.getCodec().encode(custom, ops, prefix);
    }

    public static final Codec<List<Ingredient>> LIST_CODEC_NONEMPTY = Ingredient.CODEC.listOf()
          .validate(list -> list.isEmpty() ? DataResult.error(() -> "Ingredient list cannot be empty") : DataResult.success(list));

    @SuppressWarnings("unchecked")
    public static CustomIngredientSerializer<?> serializer(IngredientType<?> type) {
        CustomIngredientSerializer<?> serializer = SERIALIZERS.get(type);
        if (serializer == null) {
            throw new IllegalStateException("Ingredient type " + type + " has not been registered");
        }
        return serializer;
    }

    /** Registers a NeoForge-format custom ingredient type with Fabric under the given id. */
    public static <T extends ICustomIngredient> CustomIngredientSerializer<T> register(String idString, IngredientType<T> type) {
        Identifier id = Identifier.parse(idString);
        @SuppressWarnings("unchecked")
        StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = (StreamCodec<RegistryFriendlyByteBuf, T>) type.streamCodec();
        CustomIngredientSerializer<T> serializer = serializer(id, type.codec(), streamCodec);
        CustomIngredientSerializer.register(serializer);
        SERIALIZERS.put(type, serializer);
        return serializer;
    }

    public static <T extends CustomIngredient> CustomIngredientSerializer<T> serializer(Identifier id, MapCodec<T> codec,
          StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return new CustomIngredientSerializer<>() {
            @Override
            public Identifier getIdentifier() {
                return id;
            }

            @Override
            public MapCodec<T> getCodec() {
                return codec;
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, T> getStreamCodec() {
                return streamCodec;
            }
        };
    }

    /** Registers the NeoForge-format custom ingredient serializers with Fabric. Called by the bootstrap. */
    public static void registerNeoForgeIngredientTypes() {
        register("neoforge:compound", CompoundIngredient.TYPE);
        register("neoforge:difference", DifferenceIngredient.TYPE);
        register("neoforge:intersection", IntersectionIngredient.TYPE);
        register("neoforge:components", DataComponentIngredient.TYPE);
    }

    private IngredientCodecs() {
    }
}
