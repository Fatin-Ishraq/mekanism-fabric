package mekanism.fabric.compat.transfer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.capability.IFluidHandler.FluidAction;
import mekanism.neoshim.neoforge.fluids.capability.IFluidHandlerItem;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A Mekanism item's fluid capability exported as a Fabric item fluid storage: each change is applied to a copy of one item and the resulting container is
 * exchanged into the context. Only whole millibuckets are moved.
 */
public final class ItemFluidExport implements Storage<FluidVariant> {

    private final ContainerItemContext context;
    private final Function<ItemStack, @Nullable IFluidHandlerItem> capability;

    public ItemFluidExport(ContainerItemContext context, Function<ItemStack, @Nullable IFluidHandlerItem> capability) {
        this.context = context;
        this.capability = capability;
    }

    @Nullable
    private IFluidHandlerItem handler(ItemStack copy) {
        return copy.isEmpty() ? null : capability.apply(copy);
    }

    private ItemStack copy() {
        return context.getItemVariant().toStack(1);
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        int mb = TransferUtil.dropletsToMb(maxAmount);
        IFluidHandlerItem handler = handler(copy());
        if (handler == null || mb <= 0 || resource.isBlank()) {
            return 0;
        }
        int filled = handler.fill(FluidHandlerStorage.toStack(resource, mb), FluidAction.EXECUTE);
        return filled > 0 && context.exchange(ItemVariant.of(handler.getContainer()), 1, transaction) == 1 ? TransferUtil.mbToDroplets(filled) : 0;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        int mb = TransferUtil.dropletsToMb(maxAmount);
        IFluidHandlerItem handler = handler(copy());
        if (handler == null || mb <= 0 || resource.isBlank()) {
            return 0;
        }
        FluidStack drained = handler.drain(FluidHandlerStorage.toStack(resource, mb), FluidAction.EXECUTE);
        return !drained.isEmpty() && context.exchange(ItemVariant.of(handler.getContainer()), 1, transaction) == 1 ? TransferUtil.mbToDroplets(drained.getAmount()) : 0;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        IFluidHandlerItem handler = handler(copy());
        List<StorageView<FluidVariant>> views = new ArrayList<>();
        if (handler != null) {
            for (int tank = 0; tank < handler.getTanks(); tank++) {
                FluidStack stack = handler.getFluidInTank(tank);
                views.add(new View(stack.isEmpty() ? FluidVariant.blank() : FluidHandlerStorage.toVariant(stack), TransferUtil.mbToDroplets(stack.getAmount()),
                      TransferUtil.mbToDroplets(handler.getTankCapacity(tank))));
            }
        }
        return views.iterator();
    }

    private final class View implements StorageView<FluidVariant> {

        private final FluidVariant resource;
        private final long amount;
        private final long capacity;

        private View(FluidVariant resource, long amount, long capacity) {
            this.resource = resource;
            this.amount = amount;
            this.capacity = capacity;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            return resource.equals(this.resource) ? ItemFluidExport.this.extract(resource, maxAmount, transaction) : 0;
        }

        @Override
        public boolean isResourceBlank() {
            return resource.isBlank();
        }

        @Override
        public FluidVariant getResource() {
            return resource;
        }

        @Override
        public long getAmount() {
            return amount;
        }

        @Override
        public long getCapacity() {
            return capacity;
        }
    }
}
