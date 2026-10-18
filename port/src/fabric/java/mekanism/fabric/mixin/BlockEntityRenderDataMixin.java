package mekanism.fabric.mixin;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;

/** Exposes NeoForge {@code getModelData()} as Fabric's block entity render data (read by NeoModelRendering on the chunk builder). */
@Mixin(BlockEntity.class)
public abstract class BlockEntityRenderDataMixin {

    public Object getRenderData() {
        return ((BlockEntity) (Object) this).getModelData();
    }
}
