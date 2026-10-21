package mekanism.neoshim.neoforge.common.extensions;

import mekanism.neoshim.neoforge.common.ItemAbility;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.core.component.DataComponents;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link ItemInstance} (item stacks and item stack templates): forwards to the item's {@link IItemExtension} hooks. */
public interface ItemInstanceExtension {

    private ItemInstance self() {
        return (ItemInstance) this;
    }

    default boolean canPerformAction(ItemAbility itemAbility) {
        return self().typeHolder().value().canPerformAction(self(), itemAbility);
    }

    default ItemEnchantments getTagEnchantments() {
        return self().getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    default int getEnchantmentLevel(Holder<Enchantment> enchantment) {
        return self().typeHolder().value().getEnchantmentLevel(self(), enchantment);
    }

    @Nullable
    default ItemStackTemplate getCraftingRemainder() {
        return self().typeHolder().value().getCraftingRemainder(self());
    }
}
