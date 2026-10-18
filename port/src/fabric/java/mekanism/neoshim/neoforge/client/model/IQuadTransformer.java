package mekanism.neoshim.neoforge.client.model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;

/** Vertex layout constants for {@link DefaultVertexFormat#BLOCK} quads, plus a simple quad transformer contract. */
@FunctionalInterface
public interface IQuadTransformer {

    int STRIDE = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
    int POSITION = 0;
    int COLOR = 3;
    int UV0 = 4;
    int UV1 = 6;
    int UV2 = 6;
    int NORMAL = 7;

    void processInPlace(BakedQuad quad);

    default void processInPlace(List<BakedQuad> quads) {
        for (BakedQuad quad : quads) {
            processInPlace(quad);
        }
    }

    default BakedQuad process(BakedQuad quad) {
        BakedQuad copy = new BakedQuad(quad.getVertices().clone(), quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
        processInPlace(copy);
        return copy;
    }
}
