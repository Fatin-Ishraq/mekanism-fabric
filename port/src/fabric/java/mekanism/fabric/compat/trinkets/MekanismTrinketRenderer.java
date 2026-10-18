package mekanism.fabric.compat.trinkets;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import mekanism.client.render.armor.ICustomArmor;
import mekanism.client.render.armor.ISpecialGear;
import mekanism.common.Mekanism;
import mekanism.neoshim.neoforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Trinkets counterpart of upstream's MekanismCurioRenderer: draws Mekanism's custom gear models (jetpacks) worn in a trinket slot. */
public record MekanismTrinketRenderer(ICustomArmor model) implements TrinketRenderer {

    @SafeVarargs
    public static void register(Holder<Item>... items) {
        for (Holder<Item> item : items) {
            if (item.value() instanceof ArmorItem armor && IClientItemExtensions.of(armor) instanceof ISpecialGear gear) {
                TrinketRendererRegistry.registerRenderer(armor, new MekanismTrinketRenderer(gear.gearModel()));
            } else {
                Mekanism.logger.warn("Attempted to register Trinkets renderer for non-special gear item: {}.", item.getRegisteredName());
            }
        }
    }

    @Override
    public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> contextModel, PoseStack matrices,
          MultiBufferSource vertexConsumers, int light, LivingEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress,
          float headYaw, float headPitch) {
        if (contextModel instanceof HumanoidModel<?> humanoidModel) {
            model.render(humanoidModel, matrices, vertexConsumers, light, OverlayTexture.NO_OVERLAY, tickDelta, stack.hasFoil(), entity, stack);
        }
    }
}
