package mekanism.neoshim.neoforge.common.extensions;

import net.minecraft.resources.Identifier;

/** Injected into {@link Identifier}. */
public interface IResourceLocationExtension {

    default int compareNamespaced(Identifier other) {
        Identifier self = (Identifier) (Object) this;
        int result = self.getNamespace().compareTo(other.getNamespace());
        return result != 0 ? result : self.getPath().compareTo(other.getPath());
    }
}
