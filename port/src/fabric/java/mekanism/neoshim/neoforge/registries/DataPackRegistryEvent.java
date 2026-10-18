package mekanism.neoshim.neoforge.registries;

import com.mojang.serialization.Codec;
import java.util.function.Consumer;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

public class DataPackRegistryEvent extends Event implements IModBusEvent {

    public static class NewRegistry extends DataPackRegistryEvent {

        public <T> void dataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec) {
            dataPackRegistry(registryKey, codec, null);
        }

        public <T> void dataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec, @Nullable Codec<T> networkCodec) {
            dataPackRegistry(registryKey, codec, networkCodec, b -> {
            });
        }

        public <T> void dataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec, @Nullable Codec<T> networkCodec,
              Consumer<RegistryBuilder<T>> consumer) {
            if (networkCodec == null) {
                DynamicRegistries.register(registryKey, codec);
            } else {
                DynamicRegistries.registerSynced(registryKey, codec, networkCodec);
            }
        }
    }
}
