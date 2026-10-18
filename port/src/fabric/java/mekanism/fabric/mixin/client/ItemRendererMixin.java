package mekanism.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.neoshim.neoforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** NeoForge: custom item renderers come from {@code IClientItemExtensions#getCustomRenderer}. */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Redirect(method = "render", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/client/renderer/BlockEntityWithoutLevelRenderer;renderByItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
    private void mekanism$customRenderer(BlockEntityWithoutLevelRenderer vanilla, ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
          MultiBufferSource buffer, int light, int overlay) {
        IClientItemExtensions extensions = IClientItemExtensions.of(stack);
        BlockEntityWithoutLevelRenderer renderer = extensions == IClientItemExtensions.DEFAULT ? vanilla : extensions.getCustomRenderer();
        renderer.renderByItem(stack, context, poseStack, buffer, light, overlay);
    }
}
