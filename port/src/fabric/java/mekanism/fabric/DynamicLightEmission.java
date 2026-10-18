package mekanism.fabric;

import mekanism.neoshim.neoforge.common.extensions.IBlockExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * NeoForge's position-aware light emission ({@link IBlockExtension#getLightEmission(BlockState, BlockGetter, BlockPos)}). Only blocks whose class overrides that
 * method are treated as dynamic, so the light engine keeps vanilla's fast path for everything else (BlockLightEngineMixin, ChunkAccessLightMixin).
 */
public final class DynamicLightEmission {

    private static final ClassValue<Boolean> DYNAMIC = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            try {
                return type.getMethod("getLightEmission", BlockState.class, BlockGetter.class, BlockPos.class).getDeclaringClass() != IBlockExtension.class;
            } catch (NoSuchMethodException e) {
                return false;
            }
        }
    };

    private DynamicLightEmission() {
    }

    public static boolean isDynamic(Block block) {
        return DYNAMIC.get(block.getClass());
    }

    public static int getEmission(BlockState state, BlockGetter level, BlockPos pos) {
        Block block = state.getBlock();
        return isDynamic(block) ? block.getLightEmission(state, level, pos) : state.getLightEmission();
    }
}
