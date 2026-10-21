package mekanism.neoshim.neoforge.client.event;

import java.util.ArrayList;
import java.util.List;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.bus.api.ICancellableEvent;
import mekanism.neoshim.neoforge.client.CustomBlockOutlineRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;

/** Posted from the bridge while the block outline is being extracted; handlers may add custom renderers in place of the vanilla outline. */
public final class ExtractBlockOutlineRenderStateEvent extends Event implements ICancellableEvent {

    private final LevelRenderer levelRenderer;
    private final ClientLevel level;
    private final BlockPos blockPos;
    private final BlockState blockState;
    private final BlockHitResult hitResult;
    private final CollisionContext collisionContext;
    private final boolean inTranslucentPass;
    private final boolean highContrast;
    private final Camera camera;
    private final LevelRenderState levelRenderState;
    private final List<CustomBlockOutlineRenderer> customRenderers = new ArrayList<>();

    public ExtractBlockOutlineRenderStateEvent(LevelRenderer levelRenderer, ClientLevel level, BlockPos blockPos, BlockState blockState, BlockHitResult hitResult,
          CollisionContext collisionContext, boolean inTranslucentPass, boolean highContrast, Camera camera, LevelRenderState levelRenderState) {
        this.levelRenderer = levelRenderer;
        this.level = level;
        this.blockPos = blockPos;
        this.blockState = blockState;
        this.hitResult = hitResult;
        this.collisionContext = collisionContext;
        this.inTranslucentPass = inTranslucentPass;
        this.highContrast = highContrast;
        this.camera = camera;
        this.levelRenderState = levelRenderState;
    }

    public void addCustomRenderer(CustomBlockOutlineRenderer renderer) {
        customRenderers.add(renderer);
    }

    public LevelRenderer getLevelRenderer() {
        return levelRenderer;
    }

    public ClientLevel getLevel() {
        return level;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public BlockHitResult getHitResult() {
        return hitResult;
    }

    public CollisionContext getCollisionContext() {
        return collisionContext;
    }

    public boolean isInTranslucentPass() {
        return inTranslucentPass;
    }

    public boolean isHighContrast() {
        return highContrast;
    }

    public Camera getCamera() {
        return camera;
    }

    public LevelRenderState getLevelRenderState() {
        return levelRenderState;
    }

    public List<CustomBlockOutlineRenderer> getCustomRenderers() {
        return customRenderers;
    }
}
