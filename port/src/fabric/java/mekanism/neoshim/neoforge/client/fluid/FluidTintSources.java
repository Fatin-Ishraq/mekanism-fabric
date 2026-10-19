package mekanism.neoshim.neoforge.client.fluid;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public final class FluidTintSources {

    private static final int DEFAULT_WATER_COLOR = 0xFF3F76E4;

    /** Adapts a block tint source; fluid stacks use the tint of the fluid's legacy block. */
    public static FluidTintSource of(BlockTintSource source) {
        if (source instanceof FluidTintSource fluidSource) {
            return fluidSource;
        }
        return new FluidTintSource() {
            @Override
            public int color(FluidState state) {
                return source.color(state.createLegacyBlock());
            }

            @Override
            public int colorInWorld(FluidState fluidState, BlockState blockState, BlockAndTintGetter level, BlockPos pos) {
                return source.colorInWorld(blockState, level, pos);
            }
        };
    }

    public static FluidTintSource constant(int color) {
        return constant(color, color);
    }

    /** {@code color} outside the world (stacks, GUIs), {@code colorInWorld} for placed fluid. */
    public static FluidTintSource constant(int color, int colorInWorld) {
        return new FluidTintSource() {
            @Override
            public int color(FluidState state) {
                return color;
            }

            @Override
            public int colorInWorld(FluidState fluidState, BlockState blockState, BlockAndTintGetter level, BlockPos pos) {
                return colorInWorld;
            }
        };
    }

    public static FluidTintSource water() {
        return new FluidTintSource() {
            @Override
            public int color(FluidState state) {
                return DEFAULT_WATER_COLOR;
            }

            @Override
            public int colorInWorld(FluidState fluidState, BlockState blockState, BlockAndTintGetter level, BlockPos pos) {
                return 0xFF000000 | BiomeColors.getAverageWaterColor(level, pos);
            }
        };
    }

    private FluidTintSources() {
    }
}
