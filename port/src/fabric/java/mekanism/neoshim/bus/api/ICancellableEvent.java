package mekanism.neoshim.bus.api;

public interface ICancellableEvent {

    default void setCanceled(boolean canceled) {
        CancellationState.set(this, canceled);
    }

    default boolean isCanceled() {
        return CancellationState.get(this);
    }
}
