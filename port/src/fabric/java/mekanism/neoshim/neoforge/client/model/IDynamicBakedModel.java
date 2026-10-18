package mekanism.neoshim.neoforge.client.model;

import java.util.List;
import mekanism.neoshim.neoforge.client.model.data.ModelData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface IDynamicBakedModel extends BakedModel {

    @Override
    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    @Override
    List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType);

    //Fabric Renderer API: render with model data and per-render-type blend modes (NOTES.md "Hook status")
    @Override
    default boolean isVanillaAdapter() {
        return false;
    }

    @Override
    default void emitBlockQuads(net.minecraft.world.level.BlockAndTintGetter blockView, BlockState state, net.minecraft.core.BlockPos pos,
          java.util.function.Supplier<RandomSource> randomSupplier, net.fabricmc.fabric.api.renderer.v1.render.RenderContext context) {
        mekanism.fabric.client.model.NeoModelRendering.emitBlockQuads(this, blockView, state, pos, randomSupplier, context);
    }

    @Override
    default void emitItemQuads(net.minecraft.world.item.ItemStack stack, java.util.function.Supplier<RandomSource> randomSupplier,
          net.fabricmc.fabric.api.renderer.v1.render.RenderContext context) {
        mekanism.fabric.client.model.NeoModelRendering.emitItemQuads(this, stack, randomSupplier, context);
    }
}
