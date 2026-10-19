package mekanism.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.neoshim.neoforge.client.event.RenderLivingEvent;
import mekanism.neoshim.neoforge.common.NeoForge;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's RenderLivingEvent.Pre/Post. */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
          at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("unchecked")
    private void mekanism$pre(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        if (NeoForge.EVENT_BUS.post(new RenderLivingEvent.Pre<>(state, (LivingEntityRenderer<T, S, M>) (Object) this, mekanism$partialTick(), poseStack, collector)).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
          at = @At("TAIL"))
    @SuppressWarnings("unchecked")
    private void mekanism$post(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new RenderLivingEvent.Post<>(state, (LivingEntityRenderer<T, S, M>) (Object) this, mekanism$partialTick(), poseStack, collector));
    }

    @org.spongepowered.asm.mixin.Unique
    private static float mekanism$partialTick() {
        return net.minecraft.client.Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }
}
