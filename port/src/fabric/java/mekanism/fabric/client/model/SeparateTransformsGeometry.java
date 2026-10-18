package mekanism.fabric.client.model;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import mekanism.neoshim.neoforge.client.model.SeparateTransformsModel;
import mekanism.neoshim.neoforge.client.model.geometry.IGeometryBakingContext;
import mekanism.neoshim.neoforge.client.model.geometry.IGeometryLoader;
import mekanism.neoshim.neoforge.client.model.geometry.IUnbakedGeometry;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

/** "neoforge:separate_transforms": a base model plus per-display-context replacement models. */
public final class SeparateTransformsGeometry implements IUnbakedGeometry<SeparateTransformsGeometry> {

    public static final IGeometryLoader<SeparateTransformsGeometry> LOADER = (json, context) -> {
        BlockModel base = NeoModelLoadingPlugin.parseInline(json.getAsJsonObject("base"));
        Map<ItemDisplayContext, BlockModel> perspectives = new HashMap<>();
        if (json.has("perspectives")) {
            for (Map.Entry<String, com.google.gson.JsonElement> entry : json.getAsJsonObject("perspectives").entrySet()) {
                ItemDisplayContext displayContext = null;
                for (ItemDisplayContext candidate : ItemDisplayContext.values()) {
                    if (candidate.getSerializedName().equals(entry.getKey())) {
                        displayContext = candidate;
                    }
                }
                if (displayContext != null) {
                    perspectives.put(displayContext, NeoModelLoadingPlugin.parseInline(entry.getValue().getAsJsonObject()));
                }
            }
        }
        return new SeparateTransformsGeometry(base, perspectives);
    };

    private final BlockModel base;
    private final Map<ItemDisplayContext, BlockModel> perspectives;

    private SeparateTransformsGeometry(BlockModel base, Map<ItemDisplayContext, BlockModel> perspectives) {
        this.base = base;
        this.perspectives = perspectives;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        base.resolveParents(modelGetter);
        perspectives.values().forEach(model -> model.resolveParents(modelGetter));
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
          ItemOverrides overrides) {
        BakedModel bakedBase = base.bake(baker, spriteGetter, modelState);
        ImmutableMap.Builder<ItemDisplayContext, BakedModel> bakedPerspectives = ImmutableMap.builder();
        perspectives.forEach((display, model) -> bakedPerspectives.put(display, model.bake(baker, spriteGetter, modelState)));
        return new SeparateTransformsModel.Baked(context.useAmbientOcclusion(), context.isGui3d(), context.useBlockLight(),
              spriteGetter.apply(context.getMaterial("particle")), overrides, bakedBase, bakedPerspectives.build());
    }
}
