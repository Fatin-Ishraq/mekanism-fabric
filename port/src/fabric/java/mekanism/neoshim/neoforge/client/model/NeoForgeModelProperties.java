package mekanism.neoshim.neoforge.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Additional model properties (root transform, part visibility, item layer face data) passed to geometry through a {@link ContextMap}. */
public final class NeoForgeModelProperties {

    public static final ContextKey<Transformation> TRANSFORM = register("transform");
    public static final ContextKey<Map<String, Boolean>> PART_VISIBILITY = register("part_visibility");
    public static final ContextKey<Int2ObjectMap<ExtraFaceData>> ITEM_LAYER_FACE_DATA = register("item_layer_face_data");

    private static Set<ContextKey<?>> keys;

    private NeoForgeModelProperties() {
    }

    private static <T> ContextKey<T> register(String name) {
        ContextKey<T> key = new ContextKey<>(Identifier.fromNamespaceAndPath("neoforge", name));
        if (keys == null) {
            keys = ConcurrentHashMap.newKeySet();
        }
        keys.add(key);
        return key;
    }

    /** Port helper: lets other code add keys to the set accepted by {@link #create(ContextMap.Builder)}. */
    public static void allowKey(ContextKey<?> key) {
        keys.add(key);
    }

    /** Port helper: builds a context map that accepts every known additional-property key. */
    public static ContextMap create(ContextMap.Builder builder) {
        ContextKeySet.Builder set = new ContextKeySet.Builder();
        for (ContextKey<?> key : keys) {
            set.optional(key);
        }
        return builder.create(set.build());
    }

    public static Transformation deserializeRootTransform(JsonObject json, JsonDeserializationContext context) {
        if (!json.has("transform")) {
            return Transformation.IDENTITY;
        }
        JsonElement element = json.get("transform");
        return Transformation.EXTENDED_CODEC.parse(JsonOps.INSTANCE, element).getOrThrow(JsonParseException::new);
    }

    public static Map<String, Boolean> deserializePartVisibility(JsonObject json) {
        Map<String, Boolean> partVisibility = new HashMap<>();
        if (json.has("visibility")) {
            JsonObject visibility = GsonHelper.getAsJsonObject(json, "visibility");
            for (Map.Entry<String, JsonElement> part : visibility.entrySet()) {
                partVisibility.put(part.getKey(), part.getValue().getAsBoolean());
            }
        }
        return partVisibility;
    }

    public static Int2ObjectMap<ExtraFaceData> deserializeItemLayerFaceData(JsonObject json) {
        Int2ObjectMap<ExtraFaceData> faceData = new Int2ObjectOpenHashMap<>();
        if (json.has("neoforge_data")) {
            JsonObject data = GsonHelper.getAsJsonObject(json, "neoforge_data");
            for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                int layer = Integer.parseInt(entry.getKey());
                faceData.put(layer, ExtraFaceData.CODEC.parse(JsonOps.INSTANCE, entry.getValue()).getOrThrow(JsonParseException::new));
            }
        }
        return faceData;
    }

    public static void fillRootTransformProperty(ContextMap.Builder builder, Transformation rootTransform) {
        if (!rootTransform.isIdentity()) {
            builder.withParameter(TRANSFORM, rootTransform);
        }
    }

    public static void fillPartVisibilityProperty(ContextMap.Builder builder, Map<String, Boolean> partVisibility) {
        if (!partVisibility.isEmpty()) {
            builder.withParameter(PART_VISIBILITY, partVisibility);
        }
    }

    public static void fillItemLayerFaceData(ContextMap.Builder builder, Int2ObjectMap<ExtraFaceData> faceData) {
        if (!faceData.isEmpty()) {
            builder.withParameter(ITEM_LAYER_FACE_DATA, faceData);
        }
    }

    public static ExtraFaceData getItemLayerFaceData(ContextMap properties, int layer) {
        Int2ObjectMap<ExtraFaceData> data = properties.getOptional(ITEM_LAYER_FACE_DATA);
        return data == null ? ExtraFaceData.DEFAULT : data.getOrDefault(layer, ExtraFaceData.DEFAULT);
    }
}
