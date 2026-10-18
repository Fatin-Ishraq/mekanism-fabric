package mekanism.neoshim.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.neoshim.bus.api.Event;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class RenderLevelStageEvent extends Event {

    private final Stage stage;
    private final LevelRenderer levelRenderer;
    private final PoseStack poseStack;
    private final Matrix4f modelViewMatrix;
    private final Matrix4f projectionMatrix;
    private final int renderTick;
    private final DeltaTracker partialTick;
    private final Camera camera;
    private final Frustum frustum;

    public RenderLevelStageEvent(Stage stage, LevelRenderer levelRenderer, PoseStack poseStack, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, int renderTick,
          DeltaTracker partialTick, Camera camera, Frustum frustum) {
        this.stage = stage;
        this.levelRenderer = levelRenderer;
        this.poseStack = poseStack;
        this.modelViewMatrix = modelViewMatrix;
        this.projectionMatrix = projectionMatrix;
        this.renderTick = renderTick;
        this.partialTick = partialTick;
        this.camera = camera;
        this.frustum = frustum;
    }

    public Stage getStage() {
        return stage;
    }

    public LevelRenderer getLevelRenderer() {
        return levelRenderer;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public Matrix4f getModelViewMatrix() {
        return modelViewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public int getRenderTick() {
        return renderTick;
    }

    public DeltaTracker getPartialTick() {
        return partialTick;
    }

    public Camera getCamera() {
        return camera;
    }

    public Frustum getFrustum() {
        return frustum;
    }

    public record Stage(ResourceLocation name) {

        public static final Stage AFTER_SKY = create("after_sky");
        public static final Stage AFTER_SOLID_BLOCKS = create("after_solid_blocks");
        public static final Stage AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS = create("after_cutout_mipped_blocks");
        public static final Stage AFTER_CUTOUT_BLOCKS = create("after_cutout_blocks");
        public static final Stage AFTER_ENTITIES = create("after_entities");
        public static final Stage AFTER_BLOCK_ENTITIES = create("after_block_entities");
        public static final Stage AFTER_TRANSLUCENT_BLOCKS = create("after_translucent_blocks");
        public static final Stage AFTER_TRIPWIRE_BLOCKS = create("after_tripwire_blocks");
        public static final Stage AFTER_PARTICLES = create("after_particles");
        public static final Stage AFTER_WEATHER = create("after_weather");
        public static final Stage AFTER_LEVEL = create("after_level");

        private static Stage create(String name) {
            return new Stage(ResourceLocation.withDefaultNamespace(name));
        }

        @Override
        public String toString() {
            return name.toString();
        }
    }
}
