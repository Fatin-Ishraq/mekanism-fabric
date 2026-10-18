package mekanism.neoshim.neoforge.client.extensions;

import net.minecraft.client.resources.model.ModelBakery;

/** Injected into {@link net.minecraft.client.resources.model.ModelManager}; the bakery is captured by ModelManagerMixin. */
public interface IModelManagerExtension {

    default ModelBakery getModelBakery() {
        throw new IllegalStateException("Implemented by mixin");
    }
}
