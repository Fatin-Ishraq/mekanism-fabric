package mekanism.neoshim.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.neoforge.client.IRenderableSection;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.joml.Matrix4fc;

public abstract class RenderLevelStageEvent extends Event {

    private final LevelRenderer levelRenderer;
    private final LevelRenderState levelRenderState;
    private final PoseStack poseStack;
    private final Matrix4fc modelViewMatrix;
    private final Iterable<? extends IRenderableSection> renderableSections;

    protected RenderLevelStageEvent(LevelRenderer levelRenderer, LevelRenderState levelRenderState, PoseStack poseStack, Matrix4fc modelViewMatrix,
          Iterable<? extends IRenderableSection> renderableSections) {
        this.levelRenderer = levelRenderer;
        this.levelRenderState = levelRenderState;
        this.poseStack = poseStack;
        this.modelViewMatrix = modelViewMatrix;
        this.renderableSections = renderableSections;
    }

    public LevelRenderer getLevelRenderer() {
        return levelRenderer;
    }

    public LevelRenderState getLevelRenderState() {
        return levelRenderState;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public Matrix4fc getModelViewMatrix() {
        return modelViewMatrix;
    }

    public Iterable<? extends IRenderableSection> getRenderableSections() {
        return renderableSections;
    }

    public static class AfterSky extends RenderLevelStageEvent {

        public AfterSky(LevelRenderer levelRenderer, LevelRenderState levelRenderState, PoseStack poseStack, Matrix4fc modelViewMatrix,
              Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections);
        }
    }

    public static class AfterOpaqueBlocks extends RenderLevelStageEvent {

        public AfterOpaqueBlocks(LevelRenderer levelRenderer, LevelRenderState levelRenderState, PoseStack poseStack, Matrix4fc modelViewMatrix,
              Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections);
        }
    }

    public static class AfterOpaqueFeatures extends RenderLevelStageEvent {

        public AfterOpaqueFeatures(LevelRenderer levelRenderer, LevelRenderState levelRenderState, PoseStack poseStack, Matrix4fc modelViewMatrix,
              Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections);
        }
    }

    public static class AfterTranslucentBlocks extends RenderLevelStageEvent {

        public AfterTranslucentBlocks(LevelRenderer levelRenderer, LevelRenderState levelRenderState, PoseStack poseStack, Matrix4fc modelViewMatrix,
              Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections);
        }
    }

    public static class AfterTranslucentFeatures extends RenderLevelStageEvent {

        public AfterTranslucentFeatures(LevelRenderer levelRenderer, LevelRenderState levelRenderState, PoseStack poseStack, Matrix4fc modelViewMatrix,
              Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections);
        }
    }

    public static class AfterTranslucentParticles extends RenderLevelStageEvent {

        public AfterTranslucentParticles(LevelRenderer levelRenderer, LevelRenderState levelRenderState, PoseStack poseStack, Matrix4fc modelViewMatrix,
              Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections);
        }
    }

    public static class AfterWeather extends RenderLevelStageEvent {

        public AfterWeather(LevelRenderer levelRenderer, LevelRenderState levelRenderState, PoseStack poseStack, Matrix4fc modelViewMatrix,
              Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections);
        }
    }

    public static class AfterLevel extends RenderLevelStageEvent {

        public AfterLevel(LevelRenderer levelRenderer, LevelRenderState levelRenderState, PoseStack poseStack, Matrix4fc modelViewMatrix,
              Iterable<? extends IRenderableSection> renderableSections) {
            super(levelRenderer, levelRenderState, poseStack, modelViewMatrix, renderableSections);
        }
    }
}
