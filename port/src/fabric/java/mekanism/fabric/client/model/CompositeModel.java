package mekanism.fabric.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import mekanism.neoshim.neoforge.client.model.AbstractUnbakedModel;
import mekanism.neoshim.neoforge.client.model.ComposedModelState;
import mekanism.neoshim.neoforge.client.model.ExtendedUnbakedGeometry;
import mekanism.neoshim.neoforge.client.model.NeoForgeModelProperties;
import mekanism.neoshim.neoforge.client.model.StandardModelParameters;
import mekanism.neoshim.neoforge.client.model.UnbakedModelLoader;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.context.ContextMap;

/**
 * Clean-room implementation of the {@code neoforge:composite} model loader: a model made of named inline child models (each a regular model JSON,
 * optionally with a {@code parent}) whose quads are merged. Children can be hidden through {@code visibility}. 26.1 picks each quad's render
 * layer from its texture, so the children's {@code render_type} is not needed and ignored.
 */
public final class CompositeModel extends AbstractUnbakedModel implements ResolvableModel {

    public static final Identifier ID = Identifier.fromNamespaceAndPath("neoforge", "composite");
    public static final UnbakedModelLoader<CompositeModel> LOADER = CompositeModel::read;

    private final Map<String, UnbakedModel> children;
    private final Geometry geometry = new Geometry();

    private CompositeModel(StandardModelParameters parameters, Map<String, UnbakedModel> children) {
        super(parameters);
        this.children = children;
    }

    private static CompositeModel read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        Map<String, UnbakedModel> children = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> child : GsonHelper.getAsJsonObject(json, "children").entrySet()) {
            children.put(child.getKey(), context.deserialize(child.getValue(), CuboidModel.class));
        }
        return new CompositeModel(StandardModelParameters.parse(json, context), children);
    }

    @Override
    public UnbakedGeometry geometry() {
        return geometry;
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        for (UnbakedModel child : children.values()) {
            if (child.parent() != null) {
                resolver.markDependency(child.parent());
            }
        }
    }

    private final class Geometry implements ExtendedUnbakedGeometry {

        @Override
        public QuadCollection bake(TextureSlots textureSlots, ModelBaker baker, ModelState state, ModelDebugName debugName, ContextMap additionalProperties) {
            Map<String, Boolean> visibility = additionalProperties.getOrDefault(NeoForgeModelProperties.PART_VISIBILITY, Map.of());
            ModelState childState = state;
            var rootTransform = additionalProperties.getOptional(NeoForgeModelProperties.TRANSFORM);
            if (rootTransform != null) {
                childState = new ComposedModelState(state, rootTransform);
            }
            QuadCollection.Builder builder = new QuadCollection.Builder();
            for (Map.Entry<String, UnbakedModel> child : children.entrySet()) {
                if (!visibility.getOrDefault(child.getKey(), true)) {
                    continue;
                }
                ModelDebugName childName = () -> debugName.debugName() + "#" + child.getKey();
                ResolvedModel resolved = baker.resolveInlineModel(child.getValue(), childName);
                builder.addAll(resolved.bakeTopGeometry(resolved.getTopTextureSlots(), baker, childState));
            }
            return builder.build();
        }
    }
}
