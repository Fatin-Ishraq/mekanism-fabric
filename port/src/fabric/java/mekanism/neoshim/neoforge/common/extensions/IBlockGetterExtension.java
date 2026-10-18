package mekanism.neoshim.neoforge.common.extensions;

import mekanism.neoshim.neoforge.model.data.ModelData;
import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

/** Injected into {@link BlockGetter}. Model data travels as Fabric block entity render data, which chunk-building regions snapshot. */
public interface IBlockGetterExtension {

    default ModelData getModelData(BlockPos pos) {
        Object data = ((FabricBlockView) this).getBlockEntityRenderData(pos);
        return data instanceof ModelData modelData ? modelData : ModelData.EMPTY;
    }
}
