package mekanism.neoshim.neoforge.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;

/**
 * Matches items whose components match a patch: {@code {"neoforge:ingredient_type": "neoforge:components", "items": ..., "components": {...}, "strict": bool}}.
 * Strict ingredients require the stack's component patch to equal the given patch exactly; otherwise every entry of the patch must hold on the stack.
 */
public class DataComponentIngredient implements ICustomIngredient {

    public static final MapCodec<DataComponentIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          Ingredient.NON_AIR_HOLDER_SET_CODEC.fieldOf("items").forGetter(DataComponentIngredient::itemSet),
          DataComponentPatch.CODEC.fieldOf("components").forGetter(DataComponentIngredient::components),
          Codec.BOOL.optionalFieldOf("strict", false).forGetter(DataComponentIngredient::componentsExhaustive)
    ).apply(builder, DataComponentIngredient::new));
    public static final IngredientType<DataComponentIngredient> TYPE = new IngredientType<>(CODEC, StreamCodec.composite(
          ByteBufCodecs.holderSet(Registries.ITEM), DataComponentIngredient::itemSet,
          DataComponentPatch.STREAM_CODEC, DataComponentIngredient::components,
          ByteBufCodecs.BOOL, DataComponentIngredient::componentsExhaustive,
          DataComponentIngredient::new));

    private final HolderSet<Item> items;
    private final DataComponentPatch components;
    private final boolean strict;

    public DataComponentIngredient(HolderSet<Item> items, DataComponentPatch components, boolean strict) {
        this.items = items;
        this.components = components;
        this.strict = strict;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!items.contains(stack.typeHolder())) {
            return false;
        }
        if (strict) {
            return stack.getComponentsPatch().equals(components);
        }
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : components.entrySet()) {
            Optional<?> expected = entry.getValue();
            Object actual = stack.get(entry.getKey());
            if (expected.isPresent() ? !expected.get().equals(actual) : actual != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Stream<Holder<Item>> items() {
        return items.stream();
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }

    @Override
    public SlotDisplay display() {
        return new SlotDisplay.Composite(items.stream()
              .map(item -> (SlotDisplay) new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(item, 1, components)))
              .toList());
    }

    public HolderSet<Item> itemSet() {
        return items;
    }

    public DataComponentPatch components() {
        return components;
    }

    public boolean componentsExhaustive() {
        return strict;
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof DataComponentIngredient other && strict == other.strict && items.equals(other.items) && components.equals(other.components);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, components, strict);
    }

    public static Ingredient of(boolean strict, ItemStack stack) {
        return of(strict, stack.getComponentsPatch(), stack.typeHolder());
    }

    public static Ingredient of(boolean strict, ItemStackTemplate stack) {
        return of(strict, stack.components(), stack.item());
    }

    public static <T> Ingredient of(DataComponentType<? super T> type, T value, ItemLike... items) {
        return of(false, type, value, items);
    }

    public static <T> Ingredient of(boolean strict, DataComponentType<? super T> type, T value, ItemLike... items) {
        return of(strict, DataComponentPatch.builder().set(type(type), value).build(), items);
    }

    public static <T> Ingredient of(boolean strict, Supplier<? extends DataComponentType<? super T>> type, T value, ItemLike... items) {
        return of(strict, type.get(), value, items);
    }

    public static Ingredient of(boolean strict, DataComponentMap map, ItemLike... items) {
        return of(strict, patchOf(map), items);
    }

    @SafeVarargs
    public static Ingredient of(boolean strict, DataComponentMap map, Holder<Item>... items) {
        return of(strict, patchOf(map), items);
    }

    public static Ingredient of(boolean strict, DataComponentMap map, HolderSet<Item> items) {
        return of(strict, patchOf(map), items);
    }

    @SafeVarargs
    public static Ingredient of(boolean strict, DataComponentPatch patch, Holder<Item>... items) {
        return of(strict, patch, HolderSet.direct(items));
    }

    public static Ingredient of(DataComponentPatch patch, ItemLike... items) {
        return of(false, patch, items);
    }

    @SuppressWarnings("deprecation")
    public static Ingredient of(boolean strict, DataComponentPatch patch, ItemLike... items) {
        return of(strict, patch, HolderSet.direct(Stream.of(items).map(item -> item.asItem().builtInRegistryHolder()).toList()));
    }

    public static Ingredient of(boolean strict, DataComponentPatch patch, HolderSet<Item> items) {
        return new DataComponentIngredient(items, patch, strict).toVanilla();
    }

    private static DataComponentPatch patchOf(DataComponentMap map) {
        return DataComponentPatch.builder().set(map).build();
    }

    @SuppressWarnings("unchecked")
    private static <T> DataComponentType<T> type(DataComponentType<? super T> type) {
        return (DataComponentType<T>) type;
    }
}
