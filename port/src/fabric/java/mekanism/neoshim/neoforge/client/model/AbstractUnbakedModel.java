package mekanism.neoshim.neoforge.client.model;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import org.jspecify.annotations.Nullable;

/** Base for custom unbaked models: exposes the {@link StandardModelParameters} through the vanilla and additional-property hooks. */
public abstract class AbstractUnbakedModel implements UnbakedModel {

    protected final StandardModelParameters parameters;

    protected AbstractUnbakedModel(StandardModelParameters parameters) {
        this.parameters = parameters;
    }

    @Nullable
    @Override
    public Boolean ambientOcclusion() {
        return parameters.ambientOcclusion();
    }

    @Nullable
    @Override
    public UnbakedModel.GuiLight guiLight() {
        return parameters.guiLight();
    }

    @Nullable
    @Override
    public ItemTransforms transforms() {
        return parameters.itemTransforms();
    }

    @Override
    public TextureSlots.Data textureSlots() {
        return parameters.textures();
    }

    @Nullable
    @Override
    public Identifier parent() {
        return parameters.parent();
    }

    @Override
    public void fillAdditionalProperties(ContextMap.Builder propertiesBuilder) {
        NeoForgeModelProperties.fillRootTransformProperty(propertiesBuilder, parameters.rootTransform());
        NeoForgeModelProperties.fillPartVisibilityProperty(propertiesBuilder, parameters.partVisibility());
    }
}
