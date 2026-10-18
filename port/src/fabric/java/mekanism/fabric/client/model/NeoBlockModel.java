package mekanism.fabric.client.model;

import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import mekanism.neoshim.neoforge.client.RenderTypeGroup;
import mekanism.neoshim.neoforge.client.model.geometry.IGeometryBakingContext;
import mekanism.neoshim.neoforge.client.model.geometry.IUnbakedGeometry;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

/**
 * A vanilla {@link BlockModel} carrying NeoForge model extensions read from its JSON: a custom geometry ({@code "loader"}), a render type
 * hint ({@code "render_type"}), part visibility ({@code "visibility"}). Being a BlockModel keeps vanilla parent resolution and lets
 * Mekanism's own code find the geometry ({@link CustomGeometryAccess}).
 */
public class NeoBlockModel extends BlockModel {

    @Nullable
    private final IUnbakedGeometry<?> geometry;
    @Nullable
    private final ResourceLocation renderTypeHint;
    private final Map<String, Boolean> visibility = new HashMap<>();
    private final Transformation rootTransform;

    public NeoBlockModel(BlockModel parsed, JsonObject json, @Nullable IUnbakedGeometry<?> geometry) {
        super(parsed.parentLocation, parsed.getElements(), parsed.textureMap, parsed.hasAmbientOcclusion, parsed.guiLight, parsed.transforms, parsed.getOverrides());
        this.name = parsed.name;
        this.geometry = geometry;
        this.renderTypeHint = json.has("render_type") ? ResourceLocation.parse(GsonHelper.getAsString(json, "render_type")) : null;
        if (json.has("visibility")) {
            for (Map.Entry<String, com.google.gson.JsonElement> entry : json.getAsJsonObject("visibility").entrySet()) {
                visibility.put(entry.getKey(), entry.getValue().getAsBoolean());
            }
        }
        this.rootTransform = Transformation.identity();
        if (geometry != null) {
            CustomGeometryAccess.setCustomGeometry(this, geometry);
        }
    }

    @Nullable
    public IUnbakedGeometry<?> getGeometry() {
        return geometry;
    }

    @Nullable
    public ResourceLocation getRenderTypeHint() {
        if (renderTypeHint != null) {
            return renderTypeHint;
        }
        return parent instanceof NeoBlockModel neoParent ? neoParent.getRenderTypeHint() : null;
    }

    public IGeometryBakingContext context() {
        return new Context();
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
        super.resolveParents(resolver);
        if (geometry != null) {
            geometry.resolveParents(resolver, context());
        }
    }

    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state) {
        if (geometry != null) {
            return geometry.bake(context(), baker, spriteGetter, state, getItemOverrides(baker, this));
        }
        BakedModel baked = super.bake(baker, spriteGetter, state);
        ResourceLocation hint = getRenderTypeHint();
        if (hint != null) {
            RenderTypeGroup group = NamedRenderTypes.get(hint);
            if (!group.isEmpty()) {
                return new RenderTypeBakedModel(baked, group);
            }
        }
        return baked;
    }

    private final class Context implements IGeometryBakingContext {

        @Override
        public String getModelName() {
            return name;
        }

        @Override
        public boolean hasMaterial(String name) {
            return hasTexture(name);
        }

        @Override
        public Material getMaterial(String name) {
            return NeoBlockModel.this.getMaterial(name);
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean useBlockLight() {
            return getGuiLight().lightLikeBlock();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return hasAmbientOcclusion();
        }

        @Override
        public ItemTransforms getTransforms() {
            return NeoBlockModel.this.getTransforms();
        }

        @Override
        public Transformation getRootTransform() {
            return rootTransform;
        }

        @Nullable
        @Override
        public ResourceLocation getRenderTypeHint() {
            return NeoBlockModel.this.getRenderTypeHint();
        }

        @Override
        public boolean isComponentVisible(String component, boolean fallback) {
            Boolean value = visibility.get(component);
            if (value == null && parent instanceof NeoBlockModel neoParent) {
                return neoParent.context().isComponentVisible(component, fallback);
            }
            return value == null ? fallback : value;
        }
    }
}
