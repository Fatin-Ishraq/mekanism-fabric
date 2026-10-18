package mekanism.neoshim.neoforge.client.extensions;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

/** Injected into {@link ParticleEngine}. */
public interface IParticleEngineExtension {

    default void addBlockHitEffects(BlockPos pos, BlockHitResult target) {
        ((ParticleEngine) this).crack(pos, target.getDirection());
    }
}
