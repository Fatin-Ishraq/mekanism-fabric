package mekanism.neoshim.neoforge.common.extensions;

import mekanism.neoshim.neoforge.capabilities.ItemCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link ItemStack}; delegates to {@link IItemExtension}. {@link ItemInstanceExtension} is injected separately via ItemInstance. */
public interface IItemStackExtension {

    private ItemStack self() {
        return (ItemStack) (Object) this;
    }

    default int getBurnTime(@Nullable RecipeType<?> recipeType, FuelValues fuelValues) {
        return self().isEmpty() ? 0 : self().getItem().getBurnTime(self(), recipeType, fuelValues);
    }

    default InteractionResult onItemUseFirst(UseOnContext context) {
        return self().getItem().onItemUseFirst(self(), context);
    }

    default boolean shouldCauseBlockBreakReset(ItemStack newStack) {
        return self().getItem().shouldCauseBlockBreakReset(self(), newStack);
    }

    default boolean isPrimaryItemFor(Holder<Enchantment> enchantment) {
        return self().getItem().isPrimaryItemFor(self(), enchantment);
    }

    default boolean supportsEnchantment(Holder<Enchantment> enchantment) {
        return self().getItem().supportsEnchantment(self(), enchantment);
    }

    default ItemEnchantments getAllEnchantments(HolderLookup.RegistryLookup<Enchantment> lookup) {
        return self().getItem().getAllEnchantments(self(), lookup);
    }

    @Nullable
    default EquipmentSlot getEquipmentSlot() {
        return self().getItem().getEquipmentSlot(self());
    }

    default boolean onEntitySwing(LivingEntity entity, InteractionHand hand) {
        return self().getItem().onEntitySwing(self(), entity, hand);
    }

    default void onStopUsing(LivingEntity entity, int count) {
        self().getItem().onStopUsing(self(), entity, count);
    }

    default int getEntityLifespan(Level level) {
        return self().getItem().getEntityLifespan(self(), level);
    }

    default boolean onEntityItemUpdate(ItemEntity entity) {
        return self().getItem().onEntityItemUpdate(self(), entity);
    }

    default float getXpRepairRatio() {
        return self().getItem().getXpRepairRatio(self());
    }

    default void onAnimalArmorTick(Level level, Mob horse) {
        self().getItem().onAnimalArmorTick(self(), level, horse);
    }

    default boolean canEquip(EquipmentSlot armorType, LivingEntity entity) {
        return self().getItem().canEquip(self(), armorType, entity);
    }

    default boolean onDroppedByPlayer(Player player) {
        return self().getItem().onDroppedByPlayer(self(), player);
    }

    default Component getHighlightTip(Component displayName) {
        return self().getItem().getHighlightTip(self(), displayName);
    }

    default boolean doesSneakBypassUse(LevelReader level, BlockPos pos, Player player) {
        return self().isEmpty() || self().getItem().doesSneakBypassUse(self(), level, pos, player);
    }

    default boolean isCombineRepairable() {
        return self().getItem().isCombineRepairable(self());
    }

    default boolean isPiglinCurrency() {
        return self().getItem().isPiglinCurrency(self());
    }

    default boolean makesPiglinsNeutral(LivingEntity wearer) {
        return self().getItem().makesPiglinsNeutral(self(), wearer);
    }

    default boolean isGazeDisguise(Player player, LivingEntity entity) {
        return self().getItem().isGazeDisguise(self(), player, entity);
    }

    default boolean canWalkOnPowderedSnow(LivingEntity wearer) {
        return self().getItem().canWalkOnPowderedSnow(self(), wearer);
    }

    default AABB getSweepHitBox(Player player, Entity target) {
        return self().getItem().getSweepHitBox(self(), player, target);
    }

    default void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
        self().getItem().onDestroyed(itemEntity, damageSource);
    }

    default boolean isNotReplaceableByPickAction(Player player, int inventorySlot) {
        return self().getItem().isNotReplaceableByPickAction(self(), player, inventorySlot);
    }

    default boolean canGrindstoneRepair() {
        return self().getItem().canGrindstoneRepair(self());
    }

    @Nullable
    default <T, C> T getCapability(ItemCapability<T, C> capability, C context) {
        return capability.getCapability(self(), context);
    }

    @Nullable
    default <T> T getCapability(ItemCapability<T, Void> capability) {
        return capability.getCapability(self(), null);
    }

    default ItemAttributeModifiers getAttributeModifiers() {
        return self().getItem().getDefaultAttributeModifiers(self());
    }

    default boolean canFitInsideContainerItems() {
        return self().getItem().canFitInsideContainerItems(self());
    }

    /** Shim helper kept for the port's own code. */
    default boolean isComponentsPatchEmpty() {
        return self().getComponentsPatch().isEmpty();
    }
}
