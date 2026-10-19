package mekanism.neoshim.neoforge.client.event;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

/** Registers into Fabric's {@link ParticleProviderRegistry}. */
public class RegisterParticleProvidersEvent extends Event implements IModBusEvent {

    public <T extends ParticleOptions> void registerSpecial(ParticleType<T> type, ParticleProvider<T> provider) {
        ParticleProviderRegistry.getInstance().register(type, provider);
    }

    public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, ParticleResources.SpriteParticleRegistration<T> registration) {
        ParticleProviderRegistry.getInstance().register(type, registration::create);
    }
}
