package mekanism.neoshim.neoforge.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * A block state model whose parts depend on the world context. Fabric renders block models through
 * {@link #emitQuads(QuadEmitter, BlockAndTintGetter, BlockPos, BlockState, RandomSource, Predicate)}, so that is routed to the context-aware
 * {@link #collectParts(BlockAndTintGetter, BlockPos, BlockState, RandomSource, List)}.
 */
public interface DynamicBlockStateModel extends BlockStateModel {

    @Override
    default void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        //Without a world context there is nothing sensible to return
    }

    @Override
    void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts);

    @Override
    default void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        collectParts(level, pos, state, random, parts);
        for (BlockStateModelPart part : parts) {
            part.emitQuads(emitter, cullTest);
        }
    }

    @Override
    default int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return materialFlags(level, pos, state);
    }
}
