package mekanism.neoshim.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;

/** Replaces part of the vanilla block outline; registered through the shim {@code ExtractBlockOutlineRenderStateEvent}. */
public interface CustomBlockOutlineRenderer {

    /// @return {@code true} if the vanilla outline should not be drawn
    boolean render(BlockOutlineRenderState renderState, MultiBufferSource.BufferSource renderer, PoseStack poseStack, boolean translucentPass,
          LevelRenderState levelRenderState);
}
