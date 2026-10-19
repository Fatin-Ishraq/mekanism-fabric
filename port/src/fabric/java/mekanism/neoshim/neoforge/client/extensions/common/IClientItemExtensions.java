package mekanism.neoshim.neoforge.client.extensions.common;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Client-side item behaviour; hooks Mekanism relies on are called by client mixins/bridges (see NOTES.md "Hook status"). */
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

    default Model getHumanoidArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original) {
        return original;
    }

    default Model getGenericArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original) {
        Model replacement = getHumanoidArmorModel(itemStack, layerType, original);
        return replacement != original ? replacement : original;
    }

    default void setupModelAnimations(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, Model model, float limbSwing,
          float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    default void renderFirstPersonOverlay(ItemStack stack, EquipmentSlot slot, Player player, GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
    }

    default boolean shouldBobAsEntity(ItemStack stack) {
        return true;
    }

    default boolean shouldSpreadAsEntity(ItemStack stack) {
        return true;
    }

    default int getArmorLayerTintColor(ItemStack stack, EquipmentClientInfo.Layer layer, int layerIdx, int fallbackColor) {
        return fallbackColor;
    }

    default int getDefaultDyeColor(ItemStack stack) {
        return 0;
    }

    default Identifier getArmorTexture(ItemStack stack, EquipmentClientInfo.LayerType type, EquipmentClientInfo.Layer layer, Identifier _default) {
        return _default;
    }

    @Nullable
    default Identifier getScopeOverlayTexture(ItemStack stack) {
        return null;
    }

    enum FontContext {
        ITEM_COUNT,
        TOOLTIP,
        SELECTED_ITEM_NAME
    }
}
