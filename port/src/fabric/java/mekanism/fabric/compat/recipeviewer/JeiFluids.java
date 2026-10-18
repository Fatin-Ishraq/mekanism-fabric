package mekanism.fabric.compat.recipeviewer;

import java.util.List;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.fabric.ingredients.fluids.JeiFluidIngredient;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

/**
 * Converts Mekanism's (NeoForge style, millibucket) fluid stacks into JEI's Fabric fluid ingredients, which count in droplets (1 mB = 81 droplets). Used by the
 * upstream JEI call sites that referenced NeoForge-only ingredient types ({@code //FABRIC:} markers). Kept separate from {@link EmiFluids} so either viewer can
 * be installed alone.
 */
public final class JeiFluids {

    public static final long DROPLETS_PER_MB = 81;

    private JeiFluids() {
    }

    public static IJeiFluidIngredient of(FluidStack stack) {
        FluidVariant variant = stack.isEmpty() ? FluidVariant.blank() : FluidVariant.of(stack.getFluid(), stack.getComponentsPatch());
        return new JeiFluidIngredient(variant, stack.getAmount() * DROPLETS_PER_MB);
    }

    public static List<IJeiFluidIngredient> of(List<FluidStack> stacks) {
        return stacks.stream().map(JeiFluids::of).toList();
    }
}
