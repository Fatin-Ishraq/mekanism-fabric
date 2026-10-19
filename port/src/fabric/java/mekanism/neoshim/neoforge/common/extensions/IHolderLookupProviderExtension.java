package mekanism.neoshim.neoforge.common.extensions;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;

/** Injected into {@link HolderLookup.Provider}. */
public interface IHolderLookupProviderExtension {

    default <T> Optional<Holder.Reference<T>> holder(ResourceKey<T> key) {
        Optional<? extends HolderLookup.RegistryLookup<T>> registry = ((HolderLookup.Provider) this).lookup(key.registryKey());
        return registry.flatMap(lookup -> lookup.get(key));
    }

    default <T> Holder<T> holderOrThrow(ResourceKey<T> key) {
        return holder(key).orElseThrow(() -> new IllegalStateException("Missing key in " + key.registryKey() + ": " + key));
    }
}
