package mekanism.neoshim.neoforge.client.extensions;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link net.minecraft.client.resources.model.ModelBaker}. */
public interface IModelBakerExtension {

    @Nullable
    default UnbakedModel getTopLevelModel(ModelResourceLocation location) {
        return null;
    }
}
