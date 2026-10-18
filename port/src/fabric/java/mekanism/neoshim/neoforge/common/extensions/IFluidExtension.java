package mekanism.neoshim.neoforge.common.extensions;

import mekanism.neoshim.neoforge.fluids.FluidType;
import net.minecraft.world.level.material.Fluid;

/** Injected into {@link Fluid}. */
public interface IFluidExtension {

    default FluidType getFluidType() {
        return FluidType.of((Fluid) this);
    }
}
