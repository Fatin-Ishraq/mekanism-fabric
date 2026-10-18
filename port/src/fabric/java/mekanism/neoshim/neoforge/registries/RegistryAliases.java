package mekanism.neoshim.neoforge.registries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge registry aliases (old id -&gt; new id), consulted by MappedRegistryAliasMixin when a lookup by id misses, so saves using renamed ids still resolve.
 */
public final class RegistryAliases {

    private static final Map<ResourceKey<? extends Registry<?>>, Map<ResourceLocation, ResourceLocation>> ALIASES = new ConcurrentHashMap<>();

    private RegistryAliases() {
    }

    public static void add(ResourceKey<? extends Registry<?>> registry, ResourceLocation from, ResourceLocation to) {
        ALIASES.computeIfAbsent(registry, r -> new ConcurrentHashMap<>()).put(from, to);
    }

    @Nullable
    public static ResourceLocation resolve(ResourceKey<? extends Registry<?>> registry, @Nullable ResourceLocation id) {
        if (id == null) {
            return null;
        }
        Map<ResourceLocation, ResourceLocation> aliases = ALIASES.get(registry);
        return aliases == null ? null : aliases.get(id);
    }
}
