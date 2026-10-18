package mekanism.neoshim.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.bus.api.ICancellableEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public abstract class RenderHighlightEvent extends Event implements ICancellableEvent {

    private final LevelRenderer levelRenderer;
    private final Camera camera;
    private final HitResult target;
    private final DeltaTracker deltaTracker;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;

    protected RenderHighlightEvent(LevelRenderer levelRenderer, Camera camera, HitResult target, DeltaTracker deltaTracker, PoseStack poseStack,
          MultiBufferSource multiBufferSource) {
        this.levelRenderer = levelRenderer;
        this.camera = camera;
        this.target = target;
        this.deltaTracker = deltaTracker;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
    }

    public LevelRenderer getLevelRenderer() {
        return levelRenderer;
    }

    public Camera getCamera() {
        return camera;
    }

    public HitResult getTarget() {
        return target;
    }

    public DeltaTracker getDeltaTracker() {
        return deltaTracker;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public MultiBufferSource getMultiBufferSource() {
        return multiBufferSource;
    }

    public static class Block extends RenderHighlightEvent {

        public Block(LevelRenderer levelRenderer, Camera camera, BlockHitResult target, DeltaTracker deltaTracker, PoseStack poseStack, MultiBufferSource bufferSource) {
            super(levelRenderer, camera, target, deltaTracker, poseStack, bufferSource);
        }

        @Override
        public BlockHitResult getTarget() {
            return (BlockHitResult) super.getTarget();
        }
    }
}
