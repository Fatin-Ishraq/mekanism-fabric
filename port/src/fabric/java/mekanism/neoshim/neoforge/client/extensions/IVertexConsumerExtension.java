package mekanism.neoshim.neoforge.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.block.model.BakedQuad;

/** Injected into {@link VertexConsumer}. */
public interface IVertexConsumerExtension {

    default VertexConsumer misc(VertexFormatElement element, int... rawData) {
        return (VertexConsumer) this;
    }

    default void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay,
          boolean readExistingColor) {
        ((VertexConsumer) this).putBulkData(pose, bakedQuad, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, red, green, blue, alpha, new int[]{packedLight, packedLight,
                                                                                                                                       packedLight, packedLight},
              packedOverlay, readExistingColor);
    }
}
