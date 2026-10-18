package mekanism.neoshim.neoforge.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.stream.Stream;
import mekanism.fabric.compat.IngredientCodecs;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/** Serialized like NeoForge: {@code {"type": "neoforge:components", "items": ..., "components": {...}, "strict": bool}}. */
public class DataComponentIngredient implements ICustomIngredient {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("neoforge", "components");
    public static final MapCodec<DataComponentIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          HolderSetCodec.create(Registries.ITEM, Item.CODEC, false).fieldOf("items").forGetter(DataComponentIngredient::items),
          DataComponentPredicate.CODEC.fieldOf("components").forGetter(DataComponentIngredient::components),
          Codec.BOOL.optionalFieldOf("strict", false).forGetter(DataComponentIngredient::isStrict)
    ).apply(builder, DataComponentIngredient::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentIngredient> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.holderSet(Registries.ITEM), DataComponentIngredient::items,
          DataComponentPredicate.STREAM_CODEC, DataComponentIngredient::components,
          ByteBufCodecs.BOOL, DataComponentIngredient::isStrict,
          DataComponentIngredient::new);
    public static final CustomIngredientSerializer<DataComponentIngredient> SERIALIZER = IngredientCodecs.serializer(ID, CODEC, STREAM_CODEC);

    private final HolderSet<Item> items;
    private final DataComponentPredicate components;
    private final boolean strict;
    private final ItemStack[] stacks;

    public DataComponentIngredient(HolderSet<Item> items, DataComponentPredicate components, boolean strict) {
        this.items = items;
        this.components = components;
        this.strict = strict;
        this.stacks = items.stream().map(item -> new ItemStack(item, 1, components.asPatch())).toArray(ItemStack[]::new);
    }

    @Override
    public boolean test(ItemStack stack) {
        if (strict) {
            for (ItemStack stack2 : this.stacks) {
                if (ItemStack.isSameItemSameComponents(stack, stack2)) {
                    return true;
                }
            }
            return false;
        }
        return this.items.contains(stack.getItemHolder()) && this.components.test(stack);
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Arrays.stream(stacks);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public HolderSet<Item> items() {
        return items;
    }

    public DataComponentPredicate components() {
        return components;
    }

    public boolean isStrict() {
        return strict;
    }

    public static Ingredient of(boolean strict, ItemStack stack) {
        return of(strict, stack.getComponents(), stack.getItem());
    }

    public static Ingredient of(boolean strict, net.minecraft.core.component.DataComponentMap map, ItemLike... items) {
        return of(strict, DataComponentPredicate.allOf(map), items);
    }

    @SafeVarargs
    public static Ingredient of(boolean strict, net.minecraft.core.component.DataComponentMap map, Holder<Item>... items) {
        return of(strict, DataComponentPredicate.allOf(map), items);
    }

    public static Ingredient of(boolean strict, net.minecraft.core.component.DataComponentMap map, HolderSet<Item> items) {
        return of(strict, DataComponentPredicate.allOf(map), items);
    }

    @SafeVarargs
    public static Ingredient of(boolean strict, DataComponentPredicate predicate, Holder<Item>... items) {
        return of(strict, predicate, HolderSet.direct(items));
    }

    @SuppressWarnings("deprecation")
    public static Ingredient of(boolean strict, DataComponentPredicate predicate, ItemLike... items) {
        return of(strict, predicate, HolderSet.direct(Arrays.stream(items).map(ItemLike::asItem).map(Item::builtInRegistryHolder).toList()));
    }

    public static Ingredient of(boolean strict, DataComponentPredicate predicate, HolderSet<Item> items) {
        return new DataComponentIngredient(items, predicate, strict).toVanilla();
    }
}
