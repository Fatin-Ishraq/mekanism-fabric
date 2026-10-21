package mekanism.neoshim.neoforge.registries;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class RegisterEvent extends Event implements IModBusEvent {

    private final ResourceKey<? extends Registry<?>> registryKey;
    private final Registry<?> registry;

    public RegisterEvent(ResourceKey<? extends Registry<?>> registryKey, Registry<?> registry) {
        this.registryKey = registryKey;
        this.registry = registry;
    }

    @SuppressWarnings("unchecked")
    public <T> void register(ResourceKey<? extends Registry<T>> registryKey, Identifier name, Supplier<T> valueSupplier) {
        if (this.registryKey.equals(registryKey)) {
            Registry.register((Registry<T>) this.registry, name, valueSupplier.get());
        }
    }

    public <T> void register(ResourceKey<? extends Registry<T>> registryKey, Consumer<RegisterHelper<T>> consumer) {
        if (this.registryKey.equals(registryKey)) {
            consumer.accept((name, value) -> register(registryKey, name, () -> value));
        }
    }

    public ResourceKey<? extends Registry<?>> getRegistryKey() {
        return this.registryKey;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> Registry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        return key == this.registryKey ? (Registry<T>) this.registry : null;
    }

    public Registry<?> getRegistry() {
        return this.registry;
    }

    @FunctionalInterface
    public interface RegisterHelper<T> {

        void register(Identifier name, T value);
    }
}
