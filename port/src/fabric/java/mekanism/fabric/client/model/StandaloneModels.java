package mekanism.fabric.client.model;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

/** NeoForge's {@code ModelResourceLocation.standalone}: extra models live under Fabric's "fabric_resource" variant (added by NeoModelLoadingPlugin). */
public final class StandaloneModels {

    public static final String VARIANT = "fabric_resource";//Fabric model loading API variant for extra models

    private StandaloneModels() {
    }

    public static ModelResourceLocation location(ResourceLocation id) {
        return new ModelResourceLocation(id, VARIANT);
    }
}
