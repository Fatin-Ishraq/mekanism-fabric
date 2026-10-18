package mekanism.neoshim.neoforge.client.extensions.common;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IClientItemExtensions {

    IClientItemExtensions DEFAULT = new IClientItemExtensions() {
    };

    static IClientItemExtensions of(ItemStack stack) {
        return of(stack.getItem());
    }

    static IClientItemExtensions of(net.minecraft.world.item.Item item) {
        return RegisterClientExtensionsEvent.ITEMS.getOrDefault(item, DEFAULT);
    }

    @Nullable
    default Font getFont(ItemStack stack, FontContext context) {
        return null;
    }

    @Nullable
    default HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
        return null;
    }

    default boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess,
          float swingProcess) {
        return false;
    }

    default HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        return original;
    }

    default Model getGenericArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        HumanoidModel<?> replacement = getHumanoidArmorModel(livingEntity, itemStack, equipmentSlot, original);
        if (replacement != original) {
            return replacement;
        }
        return original;
    }

    default void setupModelAnimations(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, Model model, float limbSwing,
          float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    default void renderHelmetOverlay(ItemStack stack, net.minecraft.world.entity.player.Player player, int width, int height, float partialTick) {
    }

    default BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return Minecraft.getInstance().getItemRenderer().getBlockEntityRenderer();
    }

    default boolean shouldBobAsEntity(ItemStack stack) {
        return true;
    }

    default boolean shouldSpreadAsEntity(ItemStack stack) {
        return true;
    }

    default int getDefaultDyeColor(ItemStack stack) {
        return 0;
    }

    enum FontContext {
        ITEM_COUNT,
        TOOLTIP,
        SELECTED_ITEM_NAME
    }
}
