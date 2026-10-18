package mekanism.neoshim.neoforge.client.model.obj;

import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import mekanism.neoshim.neoforge.client.model.geometry.IGeometryBakingContext;
import mekanism.neoshim.neoforge.client.model.geometry.IUnbakedGeometry;
import mekanism.neoshim.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Clean-room Wavefront OBJ geometry. Supports the subset Mekanism's models use: {@code v/vt/vn/f} (triangles and quads), {@code o}/{@code g}
 * parts, {@code mtllib}/{@code usemtl} with {@code Kd}, {@code d} and {@code map_Kd} (texture names starting with {@code #} are resolved through
 * the baking context's materials).
 */
public class ObjModel implements IUnbakedGeometry<ObjModel> {

    final ObjModel.ModelSettings settings;
    final List<Vector3f> positions = new ArrayList<>();
    final List<float[]> texCoords = new ArrayList<>();
    final List<Vector3f> normals = new ArrayList<>();
    final Map<String, Part> parts = new LinkedHashMap<>();
    final Map<String, ObjMaterial> materials = new LinkedHashMap<>();

    ObjModel(ModelSettings settings) {
        this.settings = settings;
    }

    public Set<String> getRootComponentNames() {
        return parts.keySet();
    }

    @Override
    public Set<String> getConfigurableComponentNames() {
        return parts.keySet();
    }

    public ResourceLocation getModelLocation() {
        return settings.modelLocation();
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
          ItemOverrides overrides) {
        TextureAtlasSprite particle = spriteGetter.apply(context.getMaterial("particle"));
        SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(context.useAmbientOcclusion(), context.useBlockLight(), context.isGui3d(),
              context.getTransforms(), overrides).particle(particle);
        Transformation transform = context.getRootTransform().isIdentity() ? modelState.getRotation()
                                                                            : modelState.getRotation().compose(context.getRootTransform());
        Matrix4f matrix = transform.isIdentity() ? null : transform.getMatrix();
        for (Map.Entry<String, Part> entry : parts.entrySet()) {
            if (!context.isComponentVisible(entry.getKey(), true)) {
                continue;
            }
            for (Mesh mesh : entry.getValue().meshes) {
                ObjMaterial material = mesh.material == null ? ObjMaterial.DEFAULT : mesh.material;
                TextureAtlasSprite sprite = spriteGetter.apply(resolveTexture(context, material.texture));
                for (int[][] face : mesh.faces) {
                    builder.addUnculledFace(bakeFace(face, material, sprite, matrix));
                }
            }
        }
        return builder.build();
    }

    private static Material resolveTexture(IGeometryBakingContext context, String texture) {
        if (texture.startsWith("#")) {
            return context.getMaterial(texture);
        }
        return new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse(texture));
    }

    private BakedQuad bakeFace(int[][] face, ObjMaterial material, TextureAtlasSprite sprite, @Nullable Matrix4f matrix) {
        QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
        consumer.setSprite(sprite);
        consumer.setShade(settings.shadeQuads());
        consumer.setTintIndex(-1);
        Vector3f[] transformed = new Vector3f[4];
        for (int i = 0; i < 4; i++) {
            Vector3f pos = new Vector3f(positions.get(face[i][0]));
            if (matrix != null) {
                Vector4f v = new Vector4f(pos.x - 0.5F, pos.y - 0.5F, pos.z - 0.5F, 1);
                matrix.transform(v);
                pos.set(v.x / v.w + 0.5F, v.y / v.w + 0.5F, v.z / v.w + 0.5F);
            }
            transformed[i] = pos;
        }
        Vector3f faceNormal = new Vector3f(transformed[2]).sub(transformed[0]).cross(new Vector3f(transformed[3]).sub(transformed[1]));
        if (faceNormal.lengthSquared() > 0) {
            faceNormal.normalize();
        }
        consumer.setDirection(Direction.getNearest(faceNormal.x, faceNormal.y, faceNormal.z));
        int r = (int) (material.color.x * 255), g = (int) (material.color.y * 255), b = (int) (material.color.z * 255), a = (int) (material.alpha * 255);
        for (int i = 0; i < 4; i++) {
            int[] index = face[i];
            Vector3f pos = transformed[i];
            float[] uv = index[1] >= 0 ? texCoords.get(index[1]) : new float[]{0, 0};
            float v = settings.flipV() ? 1 - uv[1] : uv[1];
            Vector3f normal = faceNormal;
            if (index[2] >= 0) {
                normal = new Vector3f(normals.get(index[2]));
                if (matrix != null) {
                    matrix.transformDirection(normal).normalize();
                }
            }
            consumer.addVertex(pos.x, pos.y, pos.z);
            consumer.setColor(r, g, b, a);
            consumer.setUv(sprite.getU(uv[0]), sprite.getV(v));
            consumer.setUv2(0, 0);
            consumer.setNormal(normal.x, normal.y, normal.z);
        }
        return consumer.bakeQuad();
    }

    static final class Part {

        final List<Mesh> meshes = new ArrayList<>();
    }

    static final class Mesh {

        @Nullable
        final ObjMaterial material;
        final List<int[][]> faces = new ArrayList<>();

        Mesh(@Nullable ObjMaterial material) {
            this.material = material;
        }
    }

    static final class ObjMaterial {

        static final ObjMaterial DEFAULT = new ObjMaterial("default");
        final String name;
        Vector3f color = new Vector3f(1, 1, 1);
        float alpha = 1;
        String texture = "#texture";

        ObjMaterial(String name) {
            this.name = name;
        }
    }

    public record ModelSettings(ResourceLocation modelLocation, boolean automaticCulling, boolean shadeQuads, boolean flipV, boolean emissiveAmbient,
                                @Nullable String mtlOverride) {
    }
}
