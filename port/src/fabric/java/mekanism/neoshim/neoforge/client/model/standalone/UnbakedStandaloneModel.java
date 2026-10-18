package mekanism.neoshim.neoforge.client.model.standalone;

import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvableModel;

public interface UnbakedStandaloneModel<T> extends ResolvableModel {

    T bake(ModelBaker baker, ModelDebugName debugName);
}
