package mekanism.neoshim.neoforge.transfer.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.neoshim.neoforge.transfer.resource.DataComponentHolderResource;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/** An item type plus its component patch, without a count (Fabric: {@link ItemVariant}). */
public final class ItemResource implements DataComponentHolderResource<Item> {

    public static final ItemResource EMPTY = new ItemResource(Items.AIR.builtInRegistryHolder(), DataComponentPatch.EMPTY);

    private static final Codec<ItemResource> NON_EMPTY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
          BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("id").forGetter(ItemResource::typeHolder),
          DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemResource::getComponentsPatch)
    ).apply(instance, ItemResource::of));
    public static final Codec<ItemResource> CODEC = NON_EMPTY_CODEC.validate(resource -> resource.isEmpty()
          ? com.mojang.serialization.DataResult.error(() -> "Item resource must not be empty") : com.mojang.serialization.DataResult.success(resource));
    public static final Codec<ItemResource> OPTIONAL_CODEC = NON_EMPTY_CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemResource> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.holderRegistry(Registries.ITEM), ItemResource::typeHolder,
          DataComponentPatch.STREAM_CODEC, ItemResource::getComponentsPatch,
          ItemResource::of
    );

    private final Holder<Item> item;
    private final DataComponentPatch patch;
    private DataComponentMap components;

    private ItemResource(Holder<Item> item, DataComponentPatch patch) {
        this.item = item;
        this.patch = patch;
    }

    public static ItemResource of(ItemStack stack) {
        return stack.isEmpty() ? EMPTY : of(stack.typeHolder(), stack.getComponentsPatch());
    }

    public static ItemResource of(ItemStackTemplate template) {
        return of(template.item(), template.components());
    }

    public static ItemResource of(ItemLike item) {
        return of(item.asItem().builtInRegistryHolder(), DataComponentPatch.EMPTY);
    }

    public static ItemResource of(ItemLike item, DataComponentPatch patch) {
        return of(item.asItem().builtInRegistryHolder(), patch);
    }

    public static ItemResource of(Holder<Item> item) {
        return of(item, DataComponentPatch.EMPTY);
    }

    public static ItemResource of(Holder<Item> item, DataComponentPatch patch) {
        if (item.value() == Items.AIR) {
            return EMPTY;
        }
        return new ItemResource(item, patch);
    }

    /** Conversion to/from Fabric's equivalent. */
    public static ItemResource of(ItemVariant variant) {
        return variant.isBlank() ? EMPTY : of(variant.typeHolder(), variant.getComponentsPatch());
    }

    public ItemVariant toVariant() {
        return isEmpty() ? ItemVariant.blank() : ItemVariant.of(getItem(), patch);
    }

    @Override
    public Item value() {
        return item.value();
    }

    public Item getItem() {
        return item.value();
    }

    @Override
    public Holder<Item> typeHolder() {
        return item;
    }

    @Override
    public boolean isEmpty() {
        return item.value() == Items.AIR;
    }

    public boolean matches(ItemStack stack) {
        return stack.is(item) && stack.getComponentsPatch().equals(patch);
    }

    public boolean matches(ItemStackTemplate template) {
        return template.item().value() == item.value() && template.components().equals(patch);
    }

    public boolean is(ItemLike item) {
        return getItem() == item.asItem();
    }

    public boolean test(Predicate<ItemStack> predicate) {
        return predicate.test(toStack());
    }

    @Override
    public boolean isComponentsPatchEmpty() {
        return patch.isEmpty();
    }

    @Override
    public ItemResource withMergedPatch(DataComponentPatch patch) {
        PatchedDataComponentMap map = new PatchedDataComponentMap(getItem().components());
        map.applyPatch(this.patch);
        map.applyPatch(patch);
        return of(item, map.asPatch());
    }

    @Override
    public <D> ItemResource with(DataComponentType<D> type, D value) {
        return withMergedPatch(DataComponentPatch.builder().set(type, value).build());
    }

    @Override
    public <D> ItemResource with(Supplier<? extends DataComponentType<D>> type, D value) {
        return with(type.get(), value);
    }

    @Override
    public ItemResource without(DataComponentType<?> type) {
        return withMergedPatch(DataComponentPatch.builder().remove(type).build());
    }

    @Override
    public ItemResource without(Supplier<? extends DataComponentType<?>> type) {
        return without(type.get());
    }

    @Override
    public DataComponentMap getComponents() {
        if (components == null) {
            components = PatchedDataComponentMap.fromPatch(getItem().components(), patch);
        }
        return components;
    }

    @Override
    public DataComponentPatch getComponentsPatch() {
        return patch;
    }

    public ItemStack toStack(int count) {
        if (isEmpty() || count <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item, count);
        stack.applyComponents(patch);
        return stack;
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public int getMaxStackSize() {
        return getComponents().getOrDefault(net.minecraft.core.component.DataComponents.MAX_STACK_SIZE, 1);
    }

    public Component getHoverName() {
        return toStack().getHoverName();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof ItemResource other && item.value() == other.item.value() && patch.equals(other.patch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item.value(), patch);
    }

    @Override
    public String toString() {
        return "ItemResource[" + item.getRegisteredName() + (patch.isEmpty() ? "" : " " + patch) + "]";
    }
}
