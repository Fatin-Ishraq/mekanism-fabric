package mekanism.neoshim.neoforge.common.extensions;

/** Injected into {@link net.minecraft.world.entity.LightningBolt}; implemented by LightningBoltMixin. */
public interface ILightningBoltExtension {

    default void setDamage(float damage) {
        throw new IllegalStateException("Implemented by mixin");
    }

    default float getDamage() {
        return 5.0F;
    }
}
