package mekanism.neoshim.neoforge.fluids.crafting.display;

import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.FluidType;
import net.minecraft.core.Holder;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.level.material.Fluid;

/** Display contents factory that knows how to turn fluids into display entries. */
public interface ForFluidStacks<T> extends DisplayContentsFactory<T> {

    default T forStack(Holder<Fluid> fluid) {
        return forStack(new FluidStack(fluid, FluidType.BUCKET_VOLUME));
    }

    default T forStack(Fluid fluid) {
        return forStack(fluid.builtInRegistryHolder());
    }

    T forStack(FluidStack stack);
}
