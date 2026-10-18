package mekanism.neoshim.neoforge.client.extensions;

/** Injected into {@link net.minecraft.client.renderer.block.model.BakedQuad}. Vanilla quads carry no AO flag; AO always applies. */
public interface IBakedQuadExtension {

    default boolean hasAmbientOcclusion() {
        return true;
    }
}
