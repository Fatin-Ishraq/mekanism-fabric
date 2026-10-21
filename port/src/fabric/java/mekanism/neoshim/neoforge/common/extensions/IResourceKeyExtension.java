package mekanism.neoshim.neoforge.common.extensions;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/** Injected into {@link ResourceKey}; 26.1 renamed {@code location()} to {@code identifier()}. */
public interface IResourceKeyExtension {

    default Identifier location() {
        return ((ResourceKey<?>) this).identifier();
    }
}
