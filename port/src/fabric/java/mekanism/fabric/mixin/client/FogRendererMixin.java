package mekanism.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mekanism.neoshim.neoforge.client.event.ViewportEvent;
import mekanism.neoshim.neoforge.common.NeoForge;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** NeoForge's ViewportEvent.ComputeFogColor and ViewportEvent.RenderFog (MekaSuit vision enhancement). */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Shadow
    private FogType getFogType(Camera camera) {
        throw new AssertionError();
    }

    @WrapOperation(method = "setupFog", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/client/renderer/fog/FogRenderer;computeFogColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IFLorg/joml/Vector4f;)V"))
    private void mekanism$computeFogColor(FogRenderer self, Camera camera, float partialTicks, ClientLevel level, int renderDistance, float darkenWorldAmount,
          Vector4f dest, Operation<Void> original) {
        original.call(self, camera, partialTicks, level, renderDistance, darkenWorldAmount, dest);
        NeoForge.EVENT_BUS.post(new ViewportEvent.ComputeFogColor(camera, partialTicks, dest));
    }

    @WrapOperation(method = "setupFog", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/client/renderer/fog/environment/FogEnvironment;setupFog(Lnet/minecraft/client/renderer/fog/FogData;Lnet/minecraft/client/Camera;Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/client/DeltaTracker;)V"))
    private void mekanism$renderFog(FogEnvironment environment, FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker,
          Operation<Void> original) {
        original.call(environment, fog, camera, level, renderDistance, deltaTracker);
        NeoForge.EVENT_BUS.post(new ViewportEvent.RenderFog(environment, getFogType(camera), camera, deltaTracker.getGameTimeDeltaPartialTick(false), fog));
    }
}
