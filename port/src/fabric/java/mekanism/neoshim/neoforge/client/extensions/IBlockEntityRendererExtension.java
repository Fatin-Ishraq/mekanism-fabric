package mekanism.neoshim.neoforge.client.extensions;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

/** Injected into {@link net.minecraft.client.renderer.blockentity.BlockEntityRenderer}. Culling uses it via BlockEntityRenderDispatcherMixin. */
public interface IBlockEntityRendererExtension<T extends BlockEntity> {

    default AABB getRenderBoundingBox(T blockEntity) {
        return new AABB(blockEntity.getBlockPos());
    }
}
