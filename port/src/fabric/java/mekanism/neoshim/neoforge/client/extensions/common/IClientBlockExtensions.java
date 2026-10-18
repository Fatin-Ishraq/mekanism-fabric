package mekanism.neoshim.neoforge.client.extensions.common;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public interface IClientBlockExtensions {

    IClientBlockExtensions DEFAULT = new IClientBlockExtensions() {
    };

    static IClientBlockExtensions of(BlockState state) {
        return of(state.getBlock());
    }

    static IClientBlockExtensions of(net.minecraft.world.level.block.Block block) {
        return RegisterClientExtensionsEvent.BLOCKS.getOrDefault(block, DEFAULT);
    }

    default boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
        return false;
    }

    default boolean addDestroyEffects(BlockState state, Level Level, BlockPos pos, ParticleEngine manager) {
        return !state.shouldSpawnTerrainParticles();
    }

    default boolean playBreakSound(BlockState state, Level level, BlockPos pos) {
        return false;
    }
}
