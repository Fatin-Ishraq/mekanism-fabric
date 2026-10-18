package mekanism.neoshim.neoforge.client.extensions;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.util.TriState;

/** Injected into {@link BlockStateModelPart}. */
public interface BlockStateModelPartExtension {

    default TriState ambientOcclusion() {
        return ((BlockStateModelPart) this).useAmbientOcclusion() ? TriState.DEFAULT : TriState.FALSE;
    }
}
