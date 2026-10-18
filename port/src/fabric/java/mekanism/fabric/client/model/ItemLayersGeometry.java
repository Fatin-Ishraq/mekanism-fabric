package mekanism.fabric.client.model;

import java.util.function.Function;
import mekanism.neoshim.neoforge.client.model.geometry.IGeometryBakingContext;
import mekanism.neoshim.neoforge.client.model.geometry.IGeometryLoader;
import mekanism.neoshim.neoforge.client.model.geometry.IUnbakedGeometry;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

/** "neoforge:item_layers": generated item layers. Per-layer emissivity/render types are not supported (rendered like vanilla). */
public final class ItemLayersGeometry implements IUnbakedGeometry<ItemLayersGeometry> {

    public static final IGeometryLoader<ItemLayersGeometry> LOADER = (json, context) -> new ItemLayersGeometry();

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
          ItemOverrides overrides) {
        StringBuilder textures = new StringBuilder();
        for (int i = 0; i < 16 && context.hasMaterial("layer" + i); i++) {
            if (!textures.isEmpty()) {
                textures.append(',');
            }
            textures.append("\"layer").append(i).append("\":\"").append(context.getMaterial("layer" + i).texture()).append('"');
        }
        BlockModel source = BlockModel.fromString("{\"parent\":\"minecraft:item/generated\",\"textures\":{" + textures + "}}");
        source.resolveParents(id -> baker.getModel(id));
        BlockModel generated = new ItemModelGenerator().generateBlockModel(spriteGetter, source);
        generated.resolveParents(id -> baker.getModel(id));
        return generated.bake(baker, generated, spriteGetter, modelState, false);
    }
}
