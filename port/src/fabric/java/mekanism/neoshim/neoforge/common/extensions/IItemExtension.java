package mekanism.neoshim.neoforge.common.extensions;

import java.util.function.Consumer;
import mekanism.neoshim.neoforge.common.ItemAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link Item}. Which of these vanilla actually consults on Fabric is tracked in NOTES.md "Hook status". */
public interface IItemExtension {

    private Item self() {
        return (Item) this;
    }

    default InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return InteractionResult.PASS;
    }

    default boolean isRepairable(ItemStack stack) {
        return true;
    }

    default boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return false;
    }

    default boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return false;
    }

    default boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    default boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return stack.is(Items.ENCHANTED_BOOK) || enchantment.value().isSupportedItem(stack);
    }

    default boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        if (stack.getItem() == Items.BOOK) {
            return true;
        }
        return supportsEnchantment(stack, enchantment) && enchantment.value().isPrimaryItem(stack);
    }

    default int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return stack.getTagEnchantments().getLevel(enchantment);
    }

    default ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
        return stack.getTagEnchantments();
    }

    default boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.equals(newStack);
    }

    default boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return !(newStack.is(oldStack.getItem()) && ItemStack.isSameItemSameComponents(newStack, oldStack));
    }

    default boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
        return false;
    }

    default boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return false;
    }

    default boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        return false;
    }

    @Nullable
    default EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return null;
    }

    default boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        return entity.getEquipmentSlotForItem(stack) == armorType;
    }

    default void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
        self().onDestroyed(itemEntity);
    }

    default boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return stack.is(Items.LEATHER_BOOTS);
    }

    default boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
        return stack.is(ItemTags.PIGLIN_LOVED) && stack.getItem() instanceof net.minecraft.world.item.ArmorItem;
    }

    default boolean isEnderMask(ItemStack stack, Player player, EnderMan endermanEntity) {
        return stack.getItem() == Blocks.CARVED_PUMPKIN.asItem();
    }

    default boolean isPiglinCurrency(ItemStack stack) {
        return stack.is(net.minecraft.world.entity.monster.piglin.PiglinAi.BARTERING_ITEM);
    }

    default <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        return amount;
    }

    default int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        Integer time = net.fabricmc.fabric.api.registry.FuelRegistry.INSTANCE.get(self());
        return time == null ? 0 : time;
    }

    @Nullable
    default FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        return stack.get(DataComponents.FOOD);
    }

    @Nullable
    default String getCreatorModId(ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getKey(self()).getNamespace();
    }

    default boolean hasCraftingRemainingItem(ItemStack stack) {
        return self().hasCraftingRemainingItem();
    }

    default ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        Item remainder = self().getCraftingRemainingItem();
        return remainder == null ? ItemStack.EMPTY : new ItemStack(remainder);
    }
}
