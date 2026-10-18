package mekanism.neoshim.neoforge.common.extensions;

import mekanism.neoshim.neoforge.capabilities.ItemCapability;
import mekanism.neoshim.neoforge.common.ItemAbility;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link ItemStack}; delegates to {@link IItemExtension}. */
public interface IItemStackExtension {

    private ItemStack self() {
        return (ItemStack) (Object) this;
    }

    @Nullable
    default <T> T getCapability(ItemCapability<T, Void> capability) {
        return capability.getCapability(self(), null);
    }

    @Nullable
    default <T, C> T getCapability(ItemCapability<T, C> capability, C context) {
        return capability.getCapability(self(), context);
    }

    default boolean canPerformAction(ItemAbility itemAbility) {
        return self().getItem().canPerformAction(self(), itemAbility);
    }

    default int getBurnTime(@Nullable RecipeType<?> recipeType) {
        return self().isEmpty() ? 0 : self().getItem().getBurnTime(self(), recipeType);
    }

    @Nullable
    default FoodProperties getFoodProperties(@Nullable LivingEntity entity) {
        return self().getItem().getFoodProperties(self(), entity);
    }

    default boolean canEquip(EquipmentSlot armorType, LivingEntity entity) {
        return self().getItem().canEquip(self(), armorType, entity);
    }

    default boolean hasCraftingRemainingItem() {
        return self().getItem().hasCraftingRemainingItem(self());
    }

    default ItemStack getCraftingRemainingItem() {
        return self().getItem().getCraftingRemainingItem(self());
    }

    default ItemEnchantments getTagEnchantments() {
        return self().getEnchantments();
    }

    default boolean isComponentsPatchEmpty() {
        return self().getComponentsPatch().isEmpty();
    }

    default int getEnchantmentLevel(Holder<Enchantment> enchantment) {
        return self().getItem().getEnchantmentLevel(self(), enchantment);
    }

    default ItemEnchantments getAllEnchantments(HolderLookup.RegistryLookup<Enchantment> lookup) {
        return self().getItem().getAllEnchantments(self(), lookup);
    }

    default boolean isBookEnchantable(ItemStack book) {
        return self().getItem().isBookEnchantable(self(), book);
    }

    default boolean isPrimaryItemFor(Holder<Enchantment> enchantment) {
        return self().getItem().isPrimaryItemFor(self(), enchantment);
    }

    default boolean supportsEnchantment(Holder<Enchantment> enchantment) {
        return self().getItem().supportsEnchantment(self(), enchantment);
    }

    default boolean isRepairable() {
        return self().getItem().isRepairable(self());
    }
}
