package mekanism.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.neoshim.neoforge.client.event.RenderLivingEvent;
import mekanism.neoshim.neoforge.common.NeoForge;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's RenderLivingEvent.Pre/Post. */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
          at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("unchecked")
    private void mekanism$pre(T entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        if (NeoForge.EVENT_BUS.post(new RenderLivingEvent.Pre<>(entity, (LivingEntityRenderer<T, M>) (Object) this, partialTicks, poseStack, buffer, light)).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
          at = @At("TAIL"))
    @SuppressWarnings("unchecked")
    private void mekanism$post(T entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new RenderLivingEvent.Post<>(entity, (LivingEntityRenderer<T, M>) (Object) this, partialTicks, poseStack, buffer, light));
    }
}
