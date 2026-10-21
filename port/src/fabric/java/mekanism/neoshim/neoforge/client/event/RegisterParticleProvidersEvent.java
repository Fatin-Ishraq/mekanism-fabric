package mekanism.neoshim.neoforge.client.event;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

public class RegisterParticleProvidersEvent extends Event implements IModBusEvent {

    public <T extends ParticleOptions> void registerSpecial(ParticleType<T> type, ParticleProvider<T> provider) {
        ParticleProviderRegistry.getInstance().register(type, provider);
    }

    public <T extends ParticleOptions> void registerSprite(ParticleType<T> type, ParticleProvider.Sprite<T> sprite) {
        ParticleProviderRegistry.getInstance().register(type, spriteSet -> (options, level, x, y, z, xSpeed, ySpeed, zSpeed, random) -> {
            var particle = sprite.createParticle(options, level, x, y, z, xSpeed, ySpeed, zSpeed, random);
            if (particle != null) {
                particle.setSpriteFromAge(spriteSet);
            }
            return particle;
        });
    }

    public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, SpriteParticleRegistration<T> registration) {
        ParticleProviderRegistry.getInstance().register(type, registration::create);
    }

    @FunctionalInterface
    public interface SpriteParticleRegistration<T extends ParticleOptions> {

        ParticleProvider<T> create(SpriteSet spriteSet);
    }
}
