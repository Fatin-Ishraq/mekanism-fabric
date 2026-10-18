package mekanism.neoshim.fml.event.lifecycle;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.ModContainer;
import mekanism.neoshim.fml.event.IModBusEvent;

public abstract class ModLifecycleEvent extends Event implements IModBusEvent {

    private final ModContainer container;

    protected ModLifecycleEvent(ModContainer container) {
        this.container = container;
    }

    public ModContainer getContainer() {
        return container;
    }

    /** NeoForge runs this on the main thread after parallel dispatch; on Fabric everything is already on the main thread. */
    public java.util.concurrent.CompletableFuture<Void> enqueueWork(Runnable work) {
        work.run();
        return java.util.concurrent.CompletableFuture.completedFuture(null);
    }

    public <T> java.util.concurrent.CompletableFuture<T> enqueueWork(java.util.function.Supplier<T> work) {
        return java.util.concurrent.CompletableFuture.completedFuture(work.get());
    }
}
