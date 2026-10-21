package mekanism.neoshim.neoforge.registries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge registry aliases (old id -&gt; new id), consulted by MappedRegistryAliasMixin when a lookup by id misses, so saves using renamed ids still resolve.
 */
public final class RegistryAliases {

    private static final Map<ResourceKey<? extends Registry<?>>, Map<Identifier, Identifier>> ALIASES = new ConcurrentHashMap<>();

    private RegistryAliases() {
    }

    public static void add(ResourceKey<? extends Registry<?>> registry, Identifier from, Identifier to) {
        ALIASES.computeIfAbsent(registry, r -> new ConcurrentHashMap<>()).put(from, to);
    }

    @Nullable
    public static Identifier resolve(ResourceKey<? extends Registry<?>> registry, @Nullable Identifier id) {
        if (id == null) {
            return null;
        }
        Map<Identifier, Identifier> aliases = ALIASES.get(registry);
        return aliases == null ? null : aliases.get(id);
    }
}
