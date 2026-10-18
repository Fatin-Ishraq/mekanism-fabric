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
import net.minecraft.resources.ResourceLocation;

/**
 * "neoforge:fluid_container" (filled buckets): vanilla bucket base + a fluid overlay layer (tint index 1, coloured by
 * DynamicFluidContainerModel.Colors). The overlay is a mask derived from vanilla's water bucket (scripts/make_bucket_mask.py).
 */
public final class FluidContainerGeometry implements IUnbakedGeometry<FluidContainerGeometry> {

    private static final ResourceLocation MASK = ResourceLocation.fromNamespaceAndPath("mekanism_fabric", "item/bucket_fluid_mask");
    public static final IGeometryLoader<FluidContainerGeometry> LOADER = (json, context) ->
          new FluidContainerGeometry(json.has("fluid") ? ResourceLocation.parse(json.get("fluid").getAsString()) : null);

    private final ResourceLocation fluid;

    private FluidContainerGeometry(ResourceLocation fluid) {
        this.fluid = fluid;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
          ItemOverrides overrides) {
        String json = "{\"parent\":\"minecraft:item/generated\",\"textures\":{\"layer0\":\"minecraft:item/bucket\",\"layer1\":\"" + MASK + "\"}}";
        BlockModel generatedSource = BlockModel.fromString(json);
        generatedSource.resolveParents(id -> baker.getModel(id));
        BlockModel generated = new ItemModelGenerator().generateBlockModel(spriteGetter, generatedSource);
        generated.resolveParents(id -> baker.getModel(id));
        return generated.bake(baker, generated, spriteGetter, modelState, false);
    }

}
