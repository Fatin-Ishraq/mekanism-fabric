package mekanism.fabric.client.model;

import java.util.List;
import mekanism.neoshim.neoforge.client.ChunkRenderTypeSet;
import mekanism.neoshim.neoforge.client.RenderTypeGroup;
import mekanism.neoshim.neoforge.client.model.BakedModelWrapper;
import mekanism.neoshim.neoforge.client.model.data.ModelData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** A plain baked model with a model-JSON {@code "render_type"} hint. */
public final class RenderTypeBakedModel extends BakedModelWrapper<BakedModel> {

    private final RenderTypeGroup group;
    private final ChunkRenderTypeSet blockTypes;

    public RenderTypeBakedModel(BakedModel originalModel, RenderTypeGroup group) {
        super(originalModel);
        this.group = group;
        this.blockTypes = ChunkRenderTypeSet.of(group.block());
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return blockTypes;
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        return List.of(fabulous ? group.entityFabulous() : group.entity());
    }
}
