package mekanism.fabric.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import mekanism.neoshim.neoforge.client.event.ViewportEvent;
import mekanism.neoshim.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import mekanism.neoshim.neoforge.common.NeoForge;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.world.level.material.FluidState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's ViewportEvent.ComputeFogColor and ViewportEvent.RenderFog (MekaSuit vision enhancement, custom fluid fog). */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Inject(method = "computeFogColor", at = @At("TAIL"))
    private void mekanism$computeFogColor(Camera camera, float partialTicks, ClientLevel level, int renderDistance, float bossColorModifier, Vector4f color,
          CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new ViewportEvent.ComputeFogColor(camera, partialTicks, color));
    }

    @Inject(method = "setupFog",
          at = @At(value = "INVOKE",
                target = "Lnet/minecraft/client/renderer/fog/environment/FogEnvironment;setupFog(Lnet/minecraft/client/renderer/fog/FogData;Lnet/minecraft/client/Camera;Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/client/DeltaTracker;)V",
                shift = At.Shift.AFTER))
    private void mekanism$renderFog(Camera camera, int renderDistance, DeltaTracker deltaTracker, float partialTick, ClientLevel level, CallbackInfo ci,
          @Local FogEnvironment environment, @Local FogData fogData) {
        NeoForge.EVENT_BUS.post(new ViewportEvent.RenderFog(environment, camera.getFluidInCamera(), camera, partialTick, fogData));
        FluidState fluidState = level.getFluidState(camera.blockPosition());
        if (!fluidState.isEmpty()) {
            IClientFluidTypeExtensions.of(fluidState).modifyFogRender(camera, environment, renderDistance, partialTick, fogData);
        }
    }
}
