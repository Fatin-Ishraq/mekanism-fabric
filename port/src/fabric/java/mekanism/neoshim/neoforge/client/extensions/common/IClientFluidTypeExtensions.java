package mekanism.neoshim.neoforge.client.extensions.common;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.neoshim.neoforge.fluids.FluidType;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

/** Client-side fluid type behaviour (textures and tint live in vanilla's {@code FluidModel}s since 26.1). */
public interface IClientFluidTypeExtensions {

    IClientFluidTypeExtensions DEFAULT = new IClientFluidTypeExtensions() {
    };

    static IClientFluidTypeExtensions of(FluidState state) {
        return of(state.getType());
    }

    static IClientFluidTypeExtensions of(Fluid fluid) {
        return of(FluidType.of(fluid));
    }

    static IClientFluidTypeExtensions of(FluidType type) {
        return RegisterClientExtensionsEvent.FLUID_TYPES.getOrDefault(type, DEFAULT);
    }

    @Nullable
    default Identifier getRenderOverlayTexture(Minecraft mc) {
        return null;
    }

    default void renderOverlay(Minecraft mc, PoseStack poseStack, MultiBufferSource buffers) {
    }

    default void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
    }

    default void modifyFogRender(Camera camera, @Nullable FogEnvironment environment, float renderDistance, float partialTick, FogData fog) {
    }
}
