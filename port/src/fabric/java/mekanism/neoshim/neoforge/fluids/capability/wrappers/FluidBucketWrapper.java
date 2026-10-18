package mekanism.neoshim.neoforge.fluids.capability.wrappers;

import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.FluidType;
import mekanism.neoshim.neoforge.fluids.capability.IFluidHandlerItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** Fluid handler for single-bucket items (empty bucket <-> filled bucket). */
public class FluidBucketWrapper implements IFluidHandlerItem {

    protected ItemStack container;

    public FluidBucketWrapper(ItemStack container) {
        this.container = container;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    public boolean canFillFluidType(FluidStack fluid) {
        if (fluid.getFluid() == Fluids.WATER || fluid.getFluid() == Fluids.LAVA) {
            return true;
        }
        return !fluid.getFluid().getBucket().getDefaultInstance().isEmpty() && fluid.getFluid().getBucket() != Items.AIR;
    }

    public FluidStack getFluid() {
        if (container.getItem() instanceof BucketItem bucket) {
            Fluid fluid = bucket.content;
            return fluid == Fluids.EMPTY ? FluidStack.EMPTY : new FluidStack(fluid, FluidType.BUCKET_VOLUME);
        }
        return FluidStack.EMPTY;
    }

    protected void setFluid(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            container = new ItemStack(Items.BUCKET);
        } else {
            container = new ItemStack(fluidStack.getFluid().getBucket());
        }
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return FluidType.BUCKET_VOLUME;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.getAmount() < FluidType.BUCKET_VOLUME || container.getItem() instanceof MilkBucketItem
            || !getFluid().isEmpty() || !canFillFluidType(resource)) {
            return 0;
        }
        if (action.execute()) {
            setFluid(resource);
        }
        return FluidType.BUCKET_VOLUME;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.getAmount() < FluidType.BUCKET_VOLUME) {
            return FluidStack.EMPTY;
        }
        FluidStack fluidStack = getFluid();
        if (!fluidStack.isEmpty() && FluidStack.isSameFluidSameComponents(fluidStack, resource)) {
            if (action.execute()) {
                setFluid(FluidStack.EMPTY);
            }
            return fluidStack;
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (container.getCount() != 1 || maxDrain < FluidType.BUCKET_VOLUME) {
            return FluidStack.EMPTY;
        }
        FluidStack fluidStack = getFluid();
        if (!fluidStack.isEmpty()) {
            if (action.execute()) {
                setFluid(FluidStack.EMPTY);
            }
            return fluidStack;
        }
        return FluidStack.EMPTY;
    }
}
