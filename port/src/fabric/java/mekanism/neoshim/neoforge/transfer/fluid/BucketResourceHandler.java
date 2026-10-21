package mekanism.neoshim.neoforge.transfer.fluid;

import mekanism.neoshim.neoforge.transfer.ItemAccessResourceHandler;
import mekanism.neoshim.neoforge.transfer.access.ItemAccess;
import mekanism.neoshim.neoforge.transfer.item.ItemResource;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

/** A bucket: empty, or exactly one bucket of a fluid. */
public final class BucketResourceHandler extends ItemAccessResourceHandler<FluidResource> {

    public static final int BUCKET_VOLUME = 1000;

    public BucketResourceHandler(ItemAccess itemAccess) {
        super(itemAccess, 1);
    }

    @Override
    protected FluidResource getResourceFrom(ItemResource itemResource, int index) {
        if (itemResource.getItem() instanceof BucketItem bucket && bucket.getContent() != Fluids.EMPTY) {
            return FluidResource.of(bucket.getContent());
        }
        return FluidResource.EMPTY;
    }

    @Override
    protected int getAmountFrom(ItemResource itemResource, int index) {
        return getResourceFrom(itemResource, index).isEmpty() ? 0 : BUCKET_VOLUME;
    }

    @Override
    protected ItemResource update(ItemResource itemResource, int index, FluidResource newResource, int newAmount) {
        if (newAmount == 0) {
            return ItemResource.of(Items.BUCKET);
        } else if (newAmount == BUCKET_VOLUME && newResource.isComponentsPatchEmpty() && newResource.getFluid().getBucket() != Items.AIR) {
            return ItemResource.of(newResource.getFluid().getBucket());
        }
        return ItemResource.EMPTY;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return resource.isComponentsPatchEmpty() && resource.getFluid().getBucket() != Items.AIR;
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        return BUCKET_VOLUME;
    }
}
