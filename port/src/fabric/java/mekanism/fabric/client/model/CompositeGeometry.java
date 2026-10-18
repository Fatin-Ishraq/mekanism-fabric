package mekanism.fabric.client.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import mekanism.neoshim.neoforge.client.RenderTypeGroup;
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

/** "neoforge:composite": several inline child models baked together, each with its own render type. */
public final class CompositeGeometry implements IUnbakedGeometry<CompositeGeometry> {

    public static final IGeometryLoader<CompositeGeometry> LOADER = (json, context) -> {
        Map<String, BlockModel> children = new LinkedHashMap<>();
        if (json.has("children")) {
            for (Map.Entry<String, com.google.gson.JsonElement> entry : json.getAsJsonObject("children").entrySet()) {
                children.put(entry.getKey(), NeoModelLoadingPlugin.parseInline(entry.getValue().getAsJsonObject()));
            }
        }
        List<String> itemRenderOrder = new ArrayList<>();
        if (json.has("item_render_order")) {
            json.getAsJsonArray("item_render_order").forEach(e -> itemRenderOrder.add(e.getAsString()));
        } else {
            itemRenderOrder.addAll(children.keySet());
        }
        return new CompositeGeometry(children, itemRenderOrder);
    };

    private final Map<String, BlockModel> children;
    private final List<String> itemRenderOrder;

    private CompositeGeometry(Map<String, BlockModel> children, List<String> itemRenderOrder) {
        this.children = children;
        this.itemRenderOrder = itemRenderOrder;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        for (BlockModel child : children.values()) {
            child.resolveParents(modelGetter);
        }
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
          ItemOverrides overrides) {
        Material particleLocation = context.getMaterial("particle");
        TextureAtlasSprite particle = spriteGetter.apply(particleLocation);
        List<CompositeBakedModel.Part> parts = new ArrayList<>();
        for (String name : itemRenderOrder) {
            BlockModel child = children.get(name);
            if (child == null || !context.isComponentVisible(name, true)) {
                continue;
            }
            BakedModel baked = child.bake(baker, spriteGetter, modelState);
            RenderTypeGroup group = RenderTypeGroup.EMPTY;
            ResourceLocation hint = child instanceof NeoBlockModel neo ? neo.getRenderTypeHint() : null;
            if (hint == null) {
                hint = context.getRenderTypeHint();
            }
            if (hint != null) {
                group = NamedRenderTypes.get(hint);
            }
            parts.add(new CompositeBakedModel.Part(baked, group));
        }
        return new CompositeBakedModel(parts, context.useAmbientOcclusion(), context.isGui3d(), context.useBlockLight(), particle, context.getTransforms(), overrides);
    }

}
