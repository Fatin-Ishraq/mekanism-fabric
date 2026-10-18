package mekanism.neoshim.neoforge.client.model;

import mekanism.neoshim.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.FluidType;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;

/** Bucket model tinting ("neoforge:fluid_container"); the model itself is provided by the port's model loading plugin. */
public class DynamicFluidContainerModel {

    public static class Colors implements ItemColor {

        @Override
        public int getColor(ItemStack stack, int tintIndex) {
            if (tintIndex != 1) {
                return 0xFFFFFFFF;
            }
            if (stack.getItem() instanceof BucketItem bucket) {
                FluidStack fluid = new FluidStack(bucket.content, FluidType.BUCKET_VOLUME);
                return IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
            }
            return 0xFFFFFFFF;
        }
    }
}
