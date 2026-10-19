package mekanism.neoshim.neoforge.fluids.crafting.display;

import mekanism.neoshim.neoforge.fluids.FluidStack;

/** Resolves fluid slot displays to the fluid stacks themselves. */
public class FluidStackContentsFactory implements ForFluidStacks<FluidStack> {

    public static final FluidStackContentsFactory INSTANCE = new FluidStackContentsFactory();

    @Override
    public FluidStack forStack(FluidStack stack) {
        return stack;
    }
}
