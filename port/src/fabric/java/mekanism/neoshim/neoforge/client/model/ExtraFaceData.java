package mekanism.neoshim.neoforge.client.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Extra per-face data. 26.1 quads have no per-vertex color, so only emission and shading/AO flags carry over. */
public record ExtraFaceData(int color, int blockLight, int skyLight, boolean ambientOcclusion) {

    public static final ExtraFaceData DEFAULT = new ExtraFaceData(0xFFFFFFFF, 0, 0, true);

    public static final Codec<ExtraFaceData> CODEC = RecordCodecBuilder.create(i -> i.group(
          Codec.INT.optionalFieldOf("color", 0xFFFFFFFF).forGetter(ExtraFaceData::color),
          Codec.intRange(0, 15).optionalFieldOf("block_light", 0).forGetter(ExtraFaceData::blockLight),
          Codec.intRange(0, 15).optionalFieldOf("sky_light", 0).forGetter(ExtraFaceData::skyLight),
          Codec.BOOL.optionalFieldOf("ambient_occlusion", true).forGetter(ExtraFaceData::ambientOcclusion)
    ).apply(i, ExtraFaceData::new));
}
