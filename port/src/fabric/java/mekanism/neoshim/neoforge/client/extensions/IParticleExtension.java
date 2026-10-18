package mekanism.neoshim.neoforge.client.extensions;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.AABB;

/** Injected into {@link Particle}. */
public interface IParticleExtension {

    default AABB getRenderBoundingBox(float partialTicks) {
        return ((Particle) this).getBoundingBox().inflate(1.0);
    }
}
