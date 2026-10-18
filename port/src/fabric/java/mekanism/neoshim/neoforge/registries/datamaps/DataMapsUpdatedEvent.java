package mekanism.neoshim.neoforge.registries.datamaps;

import java.util.function.Consumer;
import mekanism.neoshim.bus.api.Event;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

public class DataMapsUpdatedEvent extends Event {

    public enum UpdateCause {
        CLIENT_SYNC,
        SERVER_RELOAD
    }

    private final RegistryAccess registries;
    private final Registry<?> registry;
    private final UpdateCause cause;

    public DataMapsUpdatedEvent(RegistryAccess registries, Registry<?> registry, UpdateCause cause) {
        this.registries = registries;
        this.registry = registry;
        this.cause = cause;
    }

    public ResourceKey<? extends Registry<?>> getRegistryKey() {
        return registry.key();
    }

    public Registry<?> getRegistry() {
        return registry;
    }

    public RegistryAccess getRegistries() {
        return registries;
    }

    public UpdateCause getCause() {
        return cause;
    }

    @SuppressWarnings("unchecked")
    public <T> void ifRegistry(ResourceKey<Registry<T>> registryKey, Consumer<Registry<T>> consumer) {
        if (registryKey == registry.key()) {
            consumer.accept((Registry<T>) registry);
        }
    }
}
