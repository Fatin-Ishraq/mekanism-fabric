package mekanism.fabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Applies {@code LightningBolt#setDamage} (NeoForge) to the damage dealt by {@link Entity#thunderHit}. */
@Mixin(Entity.class)
public abstract class EntityThunderHitMixin {

    @Redirect(method = "thunderHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean mekanism$lightningDamage(Entity entity, ServerLevel level, DamageSource source, float damage, ServerLevel thunderLevel, LightningBolt bolt) {
        return entity.hurtServer(level, source, bolt.getDamage());
    }
}
