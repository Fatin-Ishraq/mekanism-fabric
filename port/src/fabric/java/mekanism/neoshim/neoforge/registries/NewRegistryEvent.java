package mekanism.neoshim.neoforge.registries;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

/** Registries built with the shim {@link RegistryBuilder} are registered on creation (Fabric), so this only validates. */
public class NewRegistryEvent extends Event implements IModBusEvent {

    public <T> void register(Registry<T> registry) {
        if (!BuiltInRegistries.REGISTRY.containsKey(registry.key().identifier())) {
            throw new IllegalStateException("Registry " + registry.key() + " was not created through RegistryBuilder");
        }
    }

    public <T> Registry<T> create(RegistryBuilder<T> builder) {
        return builder.create();
    }
}
