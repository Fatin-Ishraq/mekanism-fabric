package mekanism.fabric.client.model;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

/** NeoForge's {@code ModelResourceLocation.standalone}: extra models are keyed with the "standalone" variant and loaded by the model plugin. */
public final class StandaloneModels {

    public static final String VARIANT = "standalone";

    private StandaloneModels() {
    }

    public static ModelResourceLocation location(ResourceLocation id) {
        return new ModelResourceLocation(id, VARIANT);
    }
}
