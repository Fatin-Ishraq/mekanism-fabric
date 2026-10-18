package mekanism.neoshim.neoforge.common.extensions;

import net.minecraft.world.entity.EntityType;

/** Injected into {@link EntityType.Builder}. Velocity update syncing is decided by vanilla's tracker on Fabric; this is a no-op. */
public interface IEntityTypeBuilderExtension<T extends net.minecraft.world.entity.Entity> {

    @SuppressWarnings("unchecked")
    default EntityType.Builder<T> setShouldReceiveVelocityUpdates(boolean value) {
        return (EntityType.Builder<T>) this;
    }

    @SuppressWarnings("unchecked")
    default EntityType.Builder<T> setTrackingRange(int range) {
        return ((EntityType.Builder<T>) this).clientTrackingRange(range);
    }

    @SuppressWarnings("unchecked")
    default EntityType.Builder<T> setUpdateInterval(int interval) {
        return ((EntityType.Builder<T>) this).updateInterval(interval);
    }
}
