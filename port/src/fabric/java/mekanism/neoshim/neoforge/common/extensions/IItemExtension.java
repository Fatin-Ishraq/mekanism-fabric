package mekanism.neoshim.neoforge.common.extensions;

import java.util.List;
import java.util.function.Consumer;
import mekanism.neoshim.neoforge.common.ItemAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link Item}. Which of these vanilla actually consults on Fabric is tracked in NOTES.md "Hook status". */
public interface IItemExtension {

    private Item self() {
        return (Item) this;
    }

    default ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
    }

    default boolean onDroppedByPlayer(ItemStack item, Player player) {
        return true;
    }

    default Component getHighlightTip(ItemStack item, Component displayName) {
        return displayName;
    }

    default InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return InteractionResult.PASS;
    }

    default boolean isPiglinCurrency(ItemStack stack) {
        return stack.is(net.minecraft.world.entity.monster.piglin.PiglinAi.BARTERING_ITEM);
    }

    default boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
        return stack.is(ItemTags.PIGLIN_LOVED);
    }

    /** NeoForge declares this abstract and implements it in its Item patch; the shim keeps the per-item flag in {@link IItemPropertiesExtension}. */
    default boolean isCombineRepairable(ItemStack stack) {
        return !IItemPropertiesExtension.noCombineRepair(self()) && isDamageable(stack);
    }

    default float getXpRepairRatio(ItemStack stack) {
        return 2.0F;
    }

    default void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
    }

    default boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return false;
    }

    @Nullable
    default ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
        return self().getCraftingRemainder();
    }

    default int getEntityLifespan(ItemStack itemStack, Level level) {
        return 6000;
    }

    default boolean hasCustomEntity(ItemStack stack) {
        return false;
    }

    @Nullable
    default Entity createEntity(Level level, Entity location, ItemStack stack) {
        return null;
    }

    default boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        return false;
    }

    default boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return false;
    }

    default boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        return entity.getEquipmentSlotForItem(stack) == armorType;
    }

    @Nullable
    default EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return null;
    }

    default boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        return false;
    }

    default int getDamage(ItemStack stack) {
        return Math.clamp(stack.getOrDefault(DataComponents.DAMAGE, 0), 0, stack.getMaxDamage());
    }

    default int getMaxDamage(ItemStack stack) {
        return stack.getOrDefault(DataComponents.MAX_DAMAGE, 0);
    }

    default boolean isDamaged(ItemStack stack) {
        return stack.getDamageValue() > 0;
    }

    default void setDamage(ItemStack stack, int damage) {
        stack.set(DataComponents.DAMAGE, Math.clamp(damage, 0, stack.getMaxDamage()));
    }

    default boolean canPerformAction(ItemInstance stack, ItemAbility itemAbility) {
        return false;
    }

    default int getMaxStackSize(ItemStack stack) {
        return stack.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
    }

    default boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        if (stack.getItem() == Items.BOOK) {
            return true;
        }
        return supportsEnchantment(stack, enchantment) && enchantment.value().isPrimaryItem(stack);
    }

    default boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return stack.is(Items.ENCHANTED_BOOK) || enchantment.value().isSupportedItem(stack);
    }

    default int getEnchantmentLevel(ItemInstance stack, Holder<Enchantment> enchantment) {
        return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).getLevel(enchantment);
    }

    default ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
        return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    default boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.equals(newStack);
    }

    default boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return !(newStack.is(oldStack.getItem()) && ItemStack.isSameItemSameComponents(newStack, oldStack));
    }

    default boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return oldStack == newStack || ItemStack.isSameItemSameComponents(oldStack, newStack);
    }

    @Nullable
    default String getCreatorModId(HolderLookup.Provider registries, ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getKey(self()).getNamespace();
    }

    default int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType, FuelValues fuelValues) {
        return fuelValues.burnDuration(itemStack);
    }

    default void onAnimalArmorTick(ItemStack stack, Level level, Mob horse) {
    }

    default <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        return amount;
    }

    default void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
        self().onDestroyed(itemEntity);
    }

    default boolean isGazeDisguise(ItemStack stack, Player player, LivingEntity entity) {
        return false;
    }

    default boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return stack.is(Items.LEATHER_BOOTS);
    }

    default boolean isDamageable(ItemStack stack) {
        return stack.has(DataComponents.MAX_DAMAGE);
    }

    default AABB getSweepHitBox(ItemStack stack, Player player, Entity target) {
        return target.getBoundingBox().inflate(1.0D, 0.25D, 1.0D);
    }

    default boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
        return false;
    }

    default boolean canGrindstoneRepair(ItemStack stack) {
        return false;
    }

    default boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        return true;
    }

    default ItemStack applyEnchantments(ItemStack stack, List<EnchantmentInstance> enchantments) {
        for (EnchantmentInstance instance : enchantments) {
            stack.enchant(instance.enchantment(), instance.level());
        }
        return stack;
    }

    default boolean canFitInsideContainerItems(ItemStack stack) {
        return self().canFitInsideContainerItems();
    }
}
