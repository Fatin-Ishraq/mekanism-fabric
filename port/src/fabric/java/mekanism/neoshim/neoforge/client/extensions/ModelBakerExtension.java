package mekanism.neoshim.neoforge.client.extensions;

import mekanism.neoshim.neoforge.client.model.InlineResolvedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;

/** Injected into {@link ModelBaker}. */
public interface ModelBakerExtension {

    default ResolvedModel resolveInlineModel(UnbakedModel inlineModel, ModelDebugName debugName) {
        ModelBaker baker = (ModelBaker) this;
        return new InlineResolvedModel(inlineModel, inlineModel.parent() == null ? null : baker.getModel(inlineModel.parent()), debugName);
    }
}
