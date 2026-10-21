package mekanism.neoshim.neoforge.fluids.crafting.display;

import mekanism.neoshim.neoforge.fluids.FluidStack;

/** Returns the fluid stack as-is. */
public class FluidStackContentsFactory implements ForFluidStacks<FluidStack> {

    public static final FluidStackContentsFactory INSTANCE = new FluidStackContentsFactory();

    @Override
    public FluidStack forStack(FluidStack stack) {
        return stack;
    }
}
