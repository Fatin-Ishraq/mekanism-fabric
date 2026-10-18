package mekanism.neoshim.neoforge.common.extensions;

import net.minecraft.resources.ResourceLocation;

/** Injected into {@link ResourceLocation}. */
public interface IResourceLocationExtension {

    default int compareNamespaced(ResourceLocation other) {
        ResourceLocation self = (ResourceLocation) (Object) this;
        int result = self.getNamespace().compareTo(other.getNamespace());
        return result != 0 ? result : self.getPath().compareTo(other.getPath());
    }
}
