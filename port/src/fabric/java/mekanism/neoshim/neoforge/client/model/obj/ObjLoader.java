package mekanism.neoshim.neoforge.client.model.obj;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import mekanism.neoshim.neoforge.client.model.geometry.IGeometryLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import org.joml.Vector3f;

/** Loader for {@link ObjModel}s ("neoforge:obj"). Parsed models are cached until the next resource reload. */
public class ObjLoader implements IGeometryLoader<ObjModel>, ResourceManagerReloadListener {

    public static final ObjLoader INSTANCE = new ObjLoader();

    private final Map<ObjModel.ModelSettings, ObjModel> modelCache = new HashMap<>();
    private ResourceManager manager = Minecraft.getInstance() == null ? null : Minecraft.getInstance().getResourceManager();

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        modelCache.clear();
        manager = resourceManager;
    }

    @Override
    public ObjModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        if (!jsonObject.has("model")) {
            throw new JsonParseException("OBJ Loader requires a 'model' key that points to a valid .OBJ model.");
        }
        String modelLocation = jsonObject.get("model").getAsString();
        boolean automaticCulling = GsonHelper.getAsBoolean(jsonObject, "automatic_culling", true);
        boolean shadeQuads = GsonHelper.getAsBoolean(jsonObject, "shade_quads", true);
        boolean flipV = GsonHelper.getAsBoolean(jsonObject, "flip_v", false);
        boolean emissiveAmbient = GsonHelper.getAsBoolean(jsonObject, "emissive_ambient", true);
        String mtlOverride = GsonHelper.getAsString(jsonObject, "mtl_override", null);
        return loadModel(new ObjModel.ModelSettings(ResourceLocation.parse(modelLocation), automaticCulling, shadeQuads, flipV, emissiveAmbient, mtlOverride));
    }

    public synchronized ObjModel loadModel(ObjModel.ModelSettings settings) {
        return modelCache.computeIfAbsent(settings, s -> {
            ResourceManager resourceManager = manager != null ? manager : Minecraft.getInstance().getResourceManager();
            Resource resource = resourceManager.getResource(s.modelLocation())
                  .orElseThrow(() -> new IllegalStateException("Could not find OBJ model " + s.modelLocation()));
            try (BufferedReader reader = resource.openAsReader()) {
                return parse(s, reader, resourceManager);
            } catch (IOException e) {
                throw new RuntimeException("Could not read OBJ model " + s.modelLocation(), e);
            }
        });
    }

    private static ObjModel parse(ObjModel.ModelSettings settings, BufferedReader reader, ResourceManager resourceManager) throws IOException {
        ObjModel model = new ObjModel(settings);
        ObjModel.Part currentPart = null;
        ObjModel.Mesh currentMesh = null;
        ObjModel.ObjMaterial currentMaterial = null;
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.strip();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] tokens = line.split("\\s+");
            switch (tokens[0]) {
                case "mtllib" -> {
                    String lib = settings.mtlOverride() != null ? settings.mtlOverride() : tokens[1];
                    loadMaterials(model, settings.modelLocation(), lib, resourceManager);
                }
                case "usemtl" -> {
                    currentMaterial = model.materials.get(tokens[1]);
                    currentMesh = null;
                }
                case "v" -> model.positions.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
                case "vt" -> model.texCoords.add(new float[]{Float.parseFloat(tokens[1]), tokens.length > 2 ? Float.parseFloat(tokens[2]) : 0});
                case "vn" -> model.normals.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
                case "o", "g" -> {
                    String name = tokens.length > 1 ? tokens[1] : "default";
                    if (tokens[0].equals("o") || currentPart == null) {
                        currentPart = model.parts.computeIfAbsent(name, n -> new ObjModel.Part());
                    }
                    currentMesh = null;
                }
                case "f" -> {
                    if (currentPart == null) {
                        currentPart = model.parts.computeIfAbsent("default", n -> new ObjModel.Part());
                    }
                    if (currentMesh == null) {
                        currentMesh = new ObjModel.Mesh(currentMaterial);
                        currentPart.meshes.add(currentMesh);
                    }
                    int count = tokens.length - 1;
                    if (count < 3 || count > 4) {
                        throw new IllegalStateException("OBJ faces must be triangles or quads: " + line);
                    }
                    int[][] face = new int[4][];
                    for (int i = 0; i < 4; i++) {
                        face[i] = parseIndex(model, tokens[Math.min(i, count - 1) + 1]);
                    }
                    currentMesh.faces.add(face);
                }
                default -> {
                    //Ignore unsupported statements (s, l, etc.)
                }
            }
        }
        return model;
    }

    private static int[] parseIndex(ObjModel model, String token) {
        String[] parts = token.split("/");
        int[] result = {-1, -1, -1};
        result[0] = resolve(Integer.parseInt(parts[0]), model.positions.size());
        if (parts.length > 1 && !parts[1].isEmpty()) {
            result[1] = resolve(Integer.parseInt(parts[1]), model.texCoords.size());
        }
        if (parts.length > 2 && !parts[2].isEmpty()) {
            result[2] = resolve(Integer.parseInt(parts[2]), model.normals.size());
        }
        return result;
    }

    private static int resolve(int index, int size) {
        return index < 0 ? size + index : index - 1;
    }

    private static void loadMaterials(ObjModel model, ResourceLocation modelLocation, String lib, ResourceManager resourceManager) throws IOException {
        String path = modelLocation.getPath();
        int slash = path.lastIndexOf('/');
        ResourceLocation libLocation = lib.contains(":") ? ResourceLocation.parse(lib)
                                                         : ResourceLocation.fromNamespaceAndPath(modelLocation.getNamespace(), (slash >= 0 ? path.substring(0, slash + 1) : "") + lib);
        var resource = resourceManager.getResource(libLocation);
        if (resource.isEmpty()) {
            return;
        }
        try (BufferedReader reader = resource.get().openAsReader()) {
            ObjModel.ObjMaterial current = null;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] tokens = line.split("\\s+");
                switch (tokens[0]) {
                    case "newmtl" -> {
                        current = new ObjModel.ObjMaterial(tokens[1]);
                        model.materials.put(tokens[1], current);
                    }
                    case "Kd" -> {
                        if (current != null) {
                            current.color = new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
                        }
                    }
                    case "d" -> {
                        if (current != null) {
                            current.alpha = Float.parseFloat(tokens[1]);
                        }
                    }
                    case "map_Kd" -> {
                        if (current != null) {
                            current.texture = tokens[tokens.length - 1];
                        }
                    }
                    default -> {
                    }
                }
            }
        }
    }
}
