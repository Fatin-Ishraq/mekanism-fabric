package mekanism.fabric.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import mekanism.neoshim.neoforge.client.event.ViewportEvent;
import mekanism.neoshim.neoforge.common.NeoForge;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's ViewportEvent.ComputeFogColor and ViewportEvent.RenderFog (MekaSuit vision enhancement). */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Shadow
    private static float fogRed;
    @Shadow
    private static float fogGreen;
    @Shadow
    private static float fogBlue;

    @Inject(method = "setupColor", at = @At("TAIL"))
    private static void mekanism$computeFogColor(Camera camera, float partialTicks, ClientLevel level, int renderDistanceChunks, float bossColorModifier,
          CallbackInfo ci) {
        ViewportEvent.ComputeFogColor event = NeoForge.EVENT_BUS.post(new ViewportEvent.ComputeFogColor(camera, partialTicks, fogRed, fogGreen, fogBlue));
        if (event.getRed() != fogRed || event.getGreen() != fogGreen || event.getBlue() != fogBlue) {
            fogRed = event.getRed();
            fogGreen = event.getGreen();
            fogBlue = event.getBlue();
            RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
        }
    }

    @Inject(method = "setupFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V"))
    private static void mekanism$renderFog(Camera camera, FogRenderer.FogMode fogMode, float farPlaneDistance, boolean shouldCreateFog, float partialTick,
          CallbackInfo ci, @Local FogRenderer.FogData fogData) {
        ViewportEvent.RenderFog event = NeoForge.EVENT_BUS.post(new ViewportEvent.RenderFog(fogMode, camera.getFluidInCamera(), camera, partialTick, fogData.start,
              fogData.end, fogData.shape));
        if (event.isCanceled()) {
            fogData.start = event.getNearPlaneDistance();
            fogData.end = event.getFarPlaneDistance();
            fogData.shape = event.getFogShape();
        }
    }
}
