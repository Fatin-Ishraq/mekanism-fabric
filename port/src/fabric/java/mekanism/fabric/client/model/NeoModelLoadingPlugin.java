package mekanism.fabric.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import mekanism.common.Mekanism;
import mekanism.fabric.bridge.ModBusLifecycle;
import mekanism.neoshim.fml.ModContainer;
import mekanism.neoshim.neoforge.client.event.ModelEvent;
import mekanism.neoshim.neoforge.client.model.geometry.GeometryLoaderManager;
import mekanism.neoshim.neoforge.client.model.geometry.IGeometryLoader;
import mekanism.neoshim.neoforge.client.model.geometry.IUnbakedGeometry;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

/**
 * Fabric model-loading plugin implementing NeoForge's model extensions for our models: JSON models with {@code "loader"} or
 * {@code "render_type"} are replaced by {@link NeoBlockModel}s; extra ("standalone") models from {@code ModelEvent.RegisterAdditional} are
 * added; {@code ModelEvent.ModifyBakingResult} is applied per baked top-level model.
 */
public final class NeoModelLoadingPlugin {

    private static final Set<String> NAMESPACES = Set.of("mekanism", "mekanismgenerators", "mekanismadditions", "mekanismtools", "neoforge", "mekanism_fabric");

    private NeoModelLoadingPlugin() {
    }

    public static void register() {
        NeoForgeGeometryLoaders.register();
        PreparableModelLoadingPlugin.register((resourceManager, executor) -> CompletableFuture.supplyAsync(() -> {
            Map<ResourceLocation, JsonObject> found = new HashMap<>();
            for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources("models", path -> path.getPath().endsWith(".json")).entrySet()) {
                ResourceLocation file = entry.getKey();
                if (!NAMESPACES.contains(file.getNamespace())) {
                    continue;
                }
                try (Reader reader = entry.getValue().openAsReader()) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    if (json.has("loader") || json.has("render_type")) {
                        String path = file.getPath();
                        found.put(file.withPath(path.substring("models/".length(), path.length() - ".json".length())), json);
                    }
                } catch (Exception e) {
                    Mekanism.logger.error("[Fabric port] Failed to pre-read model {}", file, e);
                }
            }
            return found;
        }, executor), (Map<ResourceLocation, JsonObject> models, net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.Context context) -> {
            //Extra models
            Set<ModelResourceLocation> extra = new HashSet<>();
            for (ModContainer mod : ModBusLifecycle.mods()) {
                mod.getEventBus().post(new ModelEvent.RegisterAdditional(extra));
            }
            context.addModels(extra.stream().map(ModelResourceLocation::id).toList());
            //Custom geometry / render type models
            context.modifyModelOnLoad().register((model, ctx) -> {
                ResourceLocation id = ctx.resourceId();
                if (id == null || !(model instanceof BlockModel blockModel) || model instanceof NeoBlockModel) {
                    return model;
                }
                JsonObject json = models.get(id);
                if (json == null) {
                    return model;
                }
                try {
                    return new NeoBlockModel(blockModel, json, readGeometry(json));
                } catch (Exception e) {
                    Mekanism.logger.error("[Fabric port] Failed to load custom geometry for model {}", id, e);
                    return model;
                }
            });
            context.modifyModelAfterBake().register((model, ctx) -> {
                ModelResourceLocation top = ctx.topLevelId();
                if (top == null || model == null) {
                    return model;
                }
                Map<ModelResourceLocation, BakedModel> single = new HashMap<>();
                single.put(top, model);
                for (ModContainer mod : ModBusLifecycle.mods()) {
                    mod.getEventBus().post(new ModelEvent.ModifyBakingResult(single, ctx.loader()));
                }
                return single.getOrDefault(top, model);
            });
        });
    }

    /** Reads the custom geometry of a model JSON (also used for nested models, e.g. composite children). */
    public static IUnbakedGeometry<?> readGeometry(JsonObject json) {
        if (!json.has("loader")) {
            return null;
        }
        ResourceLocation loaderId = ResourceLocation.parse(json.get("loader").getAsString());
        IGeometryLoader<?> loader = GeometryLoaderManager.get(loaderId);
        if (loader == null) {
            Mekanism.logger.warn("[Fabric port] Unknown model loader {}", loaderId);
            return null;
        }
        return loader.read(json, deserializationContext());
    }

    public static JsonDeserializationContext deserializationContext() {
        return new JsonDeserializationContext() {
            @Override
            public <T> T deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT) {
                return BlockModel.GSON.fromJson(json, typeOfT);
            }
        };
    }

    /** Parses an inline model JSON (e.g. a composite child) into a vanilla/NeoBlockModel. */
    public static BlockModel parseInline(JsonObject json) {
        BlockModel parsed = BlockModel.GSON.fromJson(json, BlockModel.class);
        if (json.has("loader") || json.has("render_type")) {
            return new NeoBlockModel(parsed, json, readGeometry(json));
        }
        return parsed;
    }
}
