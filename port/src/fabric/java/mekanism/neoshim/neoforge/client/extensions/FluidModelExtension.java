package mekanism.neoshim.neoforge.client.extensions;

import mekanism.neoshim.neoforge.client.fluid.FluidTintSource;
import mekanism.neoshim.neoforge.client.fluid.FluidTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link FluidModel}: the fluid-aware view of the model's tint source. */
public interface FluidModelExtension {

    @Nullable
    default FluidTintSource fluidTintSource() {
        var source = ((FluidModel) (Object) this).tintSource();
        return source == null ? null : FluidTintSources.of(source);
    }
}
