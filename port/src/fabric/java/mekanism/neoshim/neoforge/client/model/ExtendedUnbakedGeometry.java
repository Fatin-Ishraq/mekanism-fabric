package mekanism.neoshim.neoforge.client.model;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.util.context.ContextMap;

/** Geometry that also receives the additional model properties of the model it belongs to. */
public interface ExtendedUnbakedGeometry extends UnbakedGeometry {

    @Override
    default QuadCollection bake(TextureSlots textureSlots, ModelBaker baker, ModelState state, ModelDebugName debugName) {
        ContextMap properties = debugName instanceof ResolvedModel resolved ? resolved.getTopAdditionalProperties()
                                                                             : NeoForgeModelProperties.create(new ContextMap.Builder());
        return bake(textureSlots, baker, state, debugName, properties);
    }

    @Override
    QuadCollection bake(TextureSlots textureSlots, ModelBaker baker, ModelState state, ModelDebugName debugName, ContextMap additionalProperties);
}
