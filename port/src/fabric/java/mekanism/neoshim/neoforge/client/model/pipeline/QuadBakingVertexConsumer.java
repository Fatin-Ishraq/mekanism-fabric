package mekanism.neoshim.neoforge.client.model.pipeline;

import com.mojang.blaze3d.platform.Transparency;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.Objects;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

/**
 * Collects four vertices and bakes them into a {@link BakedQuad}. 26.1 quads only store positions and UVs per vertex, so colors, normals and
 * light coordinates passed in are ignored; tint, shading, emission and the render layer live in the quad's {@link BakedQuad.MaterialInfo}.
 */
public class QuadBakingVertexConsumer implements VertexConsumer {

    private final Vector3f[] positions = new Vector3f[4];
    private final long[] uvs = new long[4];
    private int vertexIndex = -1;
    private int tintIndex = -1;
    private Direction direction = Direction.DOWN;
    @Nullable
    private TextureAtlasSprite sprite;
    @Nullable
    private ChunkSectionLayer layer;
    @Nullable
    private RenderType itemRenderType;
    @Nullable
    private Material.Baked material;
    @Nullable
    private Transparency transparency;
    private boolean shade = true;
    private int lightEmission;

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        vertexIndex++;
        if (vertexIndex >= 4) {
            throw new IllegalStateException("Too many vertices for a quad");
        }
        positions[vertexIndex] = new Vector3f(x, y, z);
        uvs[vertexIndex] = 0;
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        uvs[vertexIndex] = UVPair.pack(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer misc(VertexFormatElement element, int... rawData) {
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        return this;
    }

    public void setTintIndex(int tintIndex) {
        this.tintIndex = tintIndex;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setSprite(Material.Baked material) {
        setSprite(material, null);
    }

    public void setSprite(Material.Baked material, @Nullable Transparency transparency) {
        this.material = material;
        this.transparency = transparency;
        this.sprite = null;
    }

    public void setSprite(TextureAtlasSprite sprite, ChunkSectionLayer layer, RenderType itemRenderType) {
        this.sprite = sprite;
        this.layer = layer;
        this.itemRenderType = itemRenderType;
        this.material = null;
    }

    public void setShade(boolean shade) {
        this.shade = shade;
    }

    public void setLightEmission(int lightEmission) {
        this.lightEmission = lightEmission;
    }

    public void setAmbientOcclusion(boolean ambientOcclusion) {
        //Ambient occlusion is a per-part property in 26.1
    }

    private BakedQuad.MaterialInfo materialInfo() {
        if (material != null) {
            Transparency t = transparency;
            if (t == null) {
                t = material.forceTranslucent() ? Transparency.TRANSLUCENT : material.sprite().contents().computeTransparency(0, 0, 1, 1);
            }
            return BakedQuad.MaterialInfo.of(material, t, tintIndex, shade, lightEmission);
        }
        Objects.requireNonNull(sprite, "Sprite must be set before baking a quad");
        return new BakedQuad.MaterialInfo(sprite, Objects.requireNonNull(layer), Objects.requireNonNull(itemRenderType), tintIndex, shade, lightEmission);
    }

    public BakedQuad bakeQuad() {
        return bake(materialInfo(), v -> v);
    }

    public BakedQuad bakeQuad(ModelBaker.Interner interner) {
        return bake(interner.materialInfo(materialInfo()), interner::vector);
    }

    private BakedQuad bake(BakedQuad.MaterialInfo info, java.util.function.UnaryOperator<Vector3fc> intern) {
        if (vertexIndex != 3) {
            throw new IllegalStateException("Not enough vertices available. Vertices in buffer: " + (vertexIndex + 1));
        }
        BakedQuad quad = new BakedQuad(intern.apply(positions[0]), intern.apply(positions[1]), intern.apply(positions[2]), intern.apply(positions[3]),
              uvs[0], uvs[1], uvs[2], uvs[3], direction, info);
        vertexIndex = -1;
        return quad;
    }
}
