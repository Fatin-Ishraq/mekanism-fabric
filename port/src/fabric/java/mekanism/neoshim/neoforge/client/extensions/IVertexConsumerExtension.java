package mekanism.neoshim.neoforge.client.extensions;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;

/** Injected into {@link VertexConsumer}. */
public interface IVertexConsumerExtension {

    default VertexConsumer misc(VertexFormatElement element, int... rawData) {
        return (VertexConsumer) this;
    }
}
