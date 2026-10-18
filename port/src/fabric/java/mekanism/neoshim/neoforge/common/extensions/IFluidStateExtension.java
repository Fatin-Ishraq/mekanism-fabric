package mekanism.neoshim.neoforge.common.extensions;

import mekanism.neoshim.neoforge.fluids.FluidType;
import net.minecraft.world.level.material.FluidState;

/** Injected into {@link FluidState}. */
public interface IFluidStateExtension {

    default float getExplosionResistance(net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.Explosion explosion) {
        return ((FluidState) this).getExplosionResistance();
    }

    default FluidType getFluidType() {
        return ((FluidState) this).getType().getFluidType();
    }
}
