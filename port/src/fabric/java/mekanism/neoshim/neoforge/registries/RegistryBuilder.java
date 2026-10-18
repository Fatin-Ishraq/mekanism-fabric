package mekanism.neoshim.neoforge.registries;

import com.mojang.serialization.Lifecycle;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/** Builds a static registry via Fabric's {@link FabricRegistryBuilder}; it is added to {@code BuiltInRegistries.REGISTRY} immediately. */
public class RegistryBuilder<T> {

    private final ResourceKey<? extends Registry<T>> registryKey;
    @Nullable
    private ResourceLocation defaultKey;
    private boolean sync = false;
    private boolean intrusiveHolders = false;
    private int maxId = -1;

    public RegistryBuilder(ResourceKey<? extends Registry<T>> registryKey) {
        this.registryKey = registryKey;
    }

    public RegistryBuilder<T> defaultKey(ResourceLocation key) {
        this.defaultKey = key;
        return this;
    }

    public RegistryBuilder<T> defaultKey(ResourceKey<T> key) {
        this.defaultKey = key.location();
        return this;
    }

    public RegistryBuilder<T> sync(boolean sync) {
        this.sync = sync;
        return this;
    }

    public RegistryBuilder<T> withIntrusiveHolders() {
        this.intrusiveHolders = true;
        return this;
    }

    public RegistryBuilder<T> maxId(int maxId) {
        this.maxId = maxId;
        return this;
    }

    public RegistryBuilder<T> callback(Object callback) {
        //Registry callbacks are not supported by the shim (unused by Mekanism)
        return this;
    }

    @SuppressWarnings("unchecked")
    public Registry<T> create() {
        ResourceKey<Registry<T>> key = (ResourceKey<Registry<T>>) registryKey;
        MappedRegistry<T> registry = defaultKey == null ? new MappedRegistry<>(key, Lifecycle.stable(), intrusiveHolders)
                                                        : new DefaultedMappedRegistry<>(defaultKey.toString(), key, Lifecycle.stable(), intrusiveHolders);
        FabricRegistryBuilder<T, MappedRegistry<T>> builder = FabricRegistryBuilder.from(registry);
        if (sync) {
            builder.attribute(RegistryAttribute.SYNCED);
        }
        return builder.buildAndRegister();
    }
}
