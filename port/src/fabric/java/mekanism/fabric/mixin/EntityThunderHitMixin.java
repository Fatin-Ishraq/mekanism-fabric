package mekanism.fabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Applies {@code LightningBolt#setDamage} (NeoForge) to the damage dealt by {@link Entity#thunderHit}. */
@Mixin(Entity.class)
public abstract class EntityThunderHitMixin {

    @ModifyArg(method = "thunderHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), index = 1)
    private float mekanism$lightningDamage(float damage, ServerLevel level, LightningBolt bolt) {
        return bolt.getDamage();
    }
}
