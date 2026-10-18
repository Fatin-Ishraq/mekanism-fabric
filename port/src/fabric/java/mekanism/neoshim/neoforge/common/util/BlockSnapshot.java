package mekanism.neoshim.neoforge.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public record BlockSnapshot(ResourceKey<Level> dimension, LevelAccessor level, BlockPos pos, BlockState state, int flags) {

    public static BlockSnapshot create(ResourceKey<Level> dim, LevelAccessor world, BlockPos pos) {
        return create(dim, world, pos, 3);
    }

    public static BlockSnapshot create(ResourceKey<Level> dim, LevelAccessor world, BlockPos pos, int flag) {
        return new BlockSnapshot(dim, world, pos.immutable(), world.getBlockState(pos), flag);
    }

    public int getFlags() {
        return flags;
    }

    public boolean restore(int flags) {
        return level.setBlock(pos, state, flags);
    }

    public boolean restore() {
        return restore(flags);
    }

    public BlockState getState() {
        return state;
    }

    public BlockPos getPos() {
        return pos;
    }

    public LevelAccessor getLevel() {
        return level;
    }
}
