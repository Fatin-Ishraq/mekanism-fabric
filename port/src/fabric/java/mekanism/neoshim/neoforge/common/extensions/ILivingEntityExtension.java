package mekanism.neoshim.neoforge.common.extensions;

import mekanism.neoshim.neoforge.common.damagesource.DamageContainer;

/** Injected into {@link net.minecraft.world.entity.LivingEntity}. */
public interface ILivingEntityExtension {

    default void onDamageTaken(DamageContainer damageContainer) {
    }
}
