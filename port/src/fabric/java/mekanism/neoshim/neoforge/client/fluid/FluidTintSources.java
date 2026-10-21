package mekanism.neoshim.neoforge.client.fluid;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.material.FluidState;

public final class FluidTintSources {

    private FluidTintSources() {
    }

    public static FluidTintSource of(BlockTintSource source) {
        return source instanceof FluidTintSource fluidSource ? fluidSource : new FluidTintSource() {
            @Override
            public int color(FluidState state) {
                return source.color(state.createLegacyBlock());
            }
        };
    }

    public static FluidTintSource constant(int color) {
        return state -> color;
    }

    public static FluidTintSource constant(int color, int alpha) {
        int argb = (alpha & 0xFF) << 24 | color & 0xFFFFFF;
        return state -> argb;
    }

    public static FluidTintSource water() {
        return constant(0xFF3F76E4);
    }
}
