package mekanism.neoshim.neoforge.client.extensions.common;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.FluidType;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

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
        return RegisterClientExtensionsEvent.FLUID_TYPES.getOrDefault(type, mekanism.fabric.client.VanillaFluidClientExtensions.get(type));
    }

    default int getTintColor() {
        return 0xFFFFFFFF;
    }

    default Identifier getStillTexture() {
        return null;
    }

    default Identifier getFlowingTexture() {
        return null;
    }

    @Nullable
    default Identifier getOverlayTexture() {
        return null;
    }

    @Nullable
    default Identifier getRenderOverlayTexture(Minecraft mc) {
        return null;
    }

    default void renderOverlay(Minecraft mc, PoseStack poseStack) {
    }

    default Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
        return fluidFogColor;
    }

    default void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance,
          FogShape shape) {
    }

    default Identifier getStillTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return getStillTexture();
    }

    default Identifier getFlowingTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return getFlowingTexture();
    }

    @Nullable
    default Identifier getOverlayTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return getOverlayTexture();
    }

    default int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
        return getTintColor();
    }

    default Identifier getStillTexture(FluidStack stack) {
        return getStillTexture();
    }

    default Identifier getFlowingTexture(FluidStack stack) {
        return getFlowingTexture();
    }

    default Identifier getOverlayTexture(FluidStack stack) {
        return getOverlayTexture();
    }

    default int getTintColor(FluidStack stack) {
        return getTintColor();
    }
}
