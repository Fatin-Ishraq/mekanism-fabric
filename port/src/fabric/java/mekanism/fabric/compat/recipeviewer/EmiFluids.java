package mekanism.fabric.compat.recipeviewer;

import dev.emi.emi.api.stack.EmiStack;
import mekanism.neoshim.neoforge.fluids.FluidStack;

/** Converts Mekanism's millibucket fluid stacks into EMI stacks, which count fluids in droplets on Fabric (1 mB = 81 droplets). See {@link JeiFluids}. */
public final class EmiFluids {

    private EmiFluids() {
    }

    public static EmiStack of(FluidStack stack) {
        return stack.isEmpty() ? EmiStack.EMPTY : EmiStack.of(stack.getFluid(), stack.getComponentsPatch(), stack.getAmount() * JeiFluids.DROPLETS_PER_MB);
    }
}
