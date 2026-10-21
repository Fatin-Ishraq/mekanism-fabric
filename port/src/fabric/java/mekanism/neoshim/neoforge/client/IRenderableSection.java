package mekanism.neoshim.neoforge.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/** A renderable section of the world, as passed to {@link mekanism.neoshim.neoforge.client.event.RenderLevelStageEvent}. */
public interface IRenderableSection {

    BlockPos getRenderOrigin();

    AABB getBoundingBox();

    boolean isEmpty();
}
