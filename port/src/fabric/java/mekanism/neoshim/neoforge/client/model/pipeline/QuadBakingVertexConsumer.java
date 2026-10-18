package mekanism.neoshim.neoforge.client.model.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.neoshim.neoforge.client.model.IQuadTransformer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

/** Collects four vertices in {@code BLOCK} format and bakes them into a {@link BakedQuad}. */
public class QuadBakingVertexConsumer implements VertexConsumer {

    private final int[] quadData = new int[IQuadTransformer.STRIDE * 4];
    private int vertexIndex = -1;
    private int tintIndex = -1;
    private Direction direction = Direction.DOWN;
    private TextureAtlasSprite sprite;
    private boolean shade = true;
    private boolean hasAmbientOcclusion = true;

    private int base() {
        return vertexIndex * IQuadTransformer.STRIDE;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        vertexIndex++;
        if (vertexIndex >= 4) {
            throw new IllegalStateException("Too many vertices for a quad");
        }
        int offset = base() + IQuadTransformer.POSITION;
        quadData[offset] = Float.floatToRawIntBits(x);
        quadData[offset + 1] = Float.floatToRawIntBits(y);
        quadData[offset + 2] = Float.floatToRawIntBits(z);
        //Defaults
        quadData[base() + IQuadTransformer.COLOR] = 0xFFFFFFFF;
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        quadData[base() + IQuadTransformer.COLOR] = ((a & 0xFF) << 24) | ((b & 0xFF) << 16) | ((g & 0xFF) << 8) | (r & 0xFF);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        int offset = base() + IQuadTransformer.UV0;
        quadData[offset] = Float.floatToRawIntBits(u);
        quadData[offset + 1] = Float.floatToRawIntBits(v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        quadData[base() + IQuadTransformer.UV2] = (u & 0xFFFF) | ((v & 0xFFFF) << 16);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        int normal = ((int) (x * 127.0F) & 0xFF) | (((int) (y * 127.0F) & 0xFF) << 8) | (((int) (z * 127.0F) & 0xFF) << 16);
        quadData[base() + IQuadTransformer.NORMAL] = normal;
        return this;
    }

    public void setTintIndex(int tintIndex) {
        this.tintIndex = tintIndex;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    public void setShade(boolean shade) {
        this.shade = shade;
    }

    public void setHasAmbientOcclusion(boolean hasAmbientOcclusion) {
        this.hasAmbientOcclusion = hasAmbientOcclusion;
    }

    public BakedQuad bakeQuad() {
        if (vertexIndex != 3) {
            throw new IllegalStateException("Not enough vertices available. Vertices in buffer: " + (vertexIndex + 1));
        }
        BakedQuad quad = new BakedQuad(quadData.clone(), tintIndex, direction, sprite, shade);
        vertexIndex = -1;
        return quad;
    }
}
