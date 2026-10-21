package mekanism.neoshim.neoforge.client.extensions;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.util.context.ContextMap;

/** Injected into {@link UnbakedModel}. Extra dependencies are declared by implementing {@code ResolvableModel} (see ModelDiscoveryMixin). */
public interface UnbakedModelExtension {

    default void fillAdditionalProperties(ContextMap.Builder propertiesBuilder) {
    }
}
