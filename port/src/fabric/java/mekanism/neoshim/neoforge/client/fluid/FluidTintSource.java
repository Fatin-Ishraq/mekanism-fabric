package mekanism.neoshim.neoforge.client.fluid;

import mekanism.neoshim.neoforge.fluids.FluidStack;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/** Fluid-specific tint source, mirroring NeoForge's interface. Rendered fluids query {@link #color(FluidState)}. */
public interface FluidTintSource extends BlockTintSource {

    int color(FluidState state);

    default int colorInWorld(FluidState state, BlockState blockState, BlockAndTintGetter level, BlockPos pos) {
        return color(state);
    }

    default int colorAsStack(FluidStack stack) {
        return stack.isEmpty() ? 0xFFFFFFFF : color(stack.getFluid().defaultFluidState());
    }

    @Override
    default int color(BlockState state) {
        return color(state.getFluidState());
    }

    @Override
    default int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return colorInWorld(state.getFluidState(), state, level, pos);
    }
}
