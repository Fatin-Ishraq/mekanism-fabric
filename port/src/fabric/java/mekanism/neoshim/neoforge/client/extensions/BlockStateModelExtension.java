package mekanism.neoshim.neoforge.client.extensions;

import java.util.List;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Injected into {@link BlockStateModel}. {@code createGeometryKey} and {@code particleMaterial(level, pos, state)} have identical signatures in
 * Fabric's {@code FabricBlockStateModel}, which BlockStateModel already extends, so they are not redeclared here.
 */
public interface BlockStateModelExtension {

    private BlockStateModel self() {
        return (BlockStateModel) this;
    }

    default void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        self().collectParts(random, parts);
    }

    default int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return self().materialFlags();
    }

    default boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, int flag) {
        return (materialFlags(level, pos, state) & flag) != 0;
    }
}
