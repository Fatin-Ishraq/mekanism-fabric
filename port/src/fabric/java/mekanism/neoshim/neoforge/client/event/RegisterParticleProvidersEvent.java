package mekanism.neoshim.neoforge.client.event;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

public class RegisterParticleProvidersEvent extends Event implements IModBusEvent {

    public <T extends ParticleOptions> void registerSpecial(ParticleType<T> type, ParticleProvider<T> provider) {
        ParticleFactoryRegistry.getInstance().register(type, provider);
    }

    public <T extends ParticleOptions> void registerSprite(ParticleType<T> type, ParticleProvider.Sprite<T> sprite) {
        ParticleFactoryRegistry.getInstance().register(type, spriteSet -> (options, level, x, y, z, xSpeed, ySpeed, zSpeed) -> {
            var particle = sprite.createParticle(options, level, x, y, z, xSpeed, ySpeed, zSpeed);
            if (particle != null) {
                particle.pickSprite(spriteSet);
            }
            return particle;
        });
    }

    public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, ParticleEngine.SpriteParticleRegistration<T> registration) {
        ParticleFactoryRegistry.getInstance().register(type, registration::create);
    }
}
