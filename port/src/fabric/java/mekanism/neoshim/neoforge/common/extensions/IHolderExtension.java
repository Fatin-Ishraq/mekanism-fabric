package mekanism.neoshim.neoforge.common.extensions;

import mekanism.neoshim.neoforge.registries.datamaps.IWithData;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link Holder}. */
public interface IHolderExtension<T> extends IWithData<T> {

    @Nullable
    @SuppressWarnings("unchecked")
    default ResourceKey<T> getKey() {
        return ((Holder<T>) this).unwrapKey().orElse(null);
    }

    @SuppressWarnings("unchecked")
    default Holder<T> getDelegate() {
        return (Holder<T>) this;
    }
}
