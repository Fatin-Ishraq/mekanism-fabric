package mekanism.neoshim.neoforge.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.ARGB;

/** Injected into {@link VertexConsumer}. */
public interface IVertexConsumerExtension {

    default VertexConsumer misc(VertexFormatElement element, int... rawData) {
        return (VertexConsumer) this;
    }

    default void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
        QuadInstance instance = new QuadInstance();
        instance.setColor(ARGB.colorFromFloat(alpha, red, green, blue));
        instance.setLightCoords(packedLight);
        instance.setOverlayCoords(packedOverlay);
        ((VertexConsumer) this).putBakedQuad(pose, bakedQuad, instance);
    }
}
