package mekanism.fabric.compat.transfer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.capability.IFluidHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/** Exposes an {@link IFluidHandler} (mB) as a Fabric {@code Storage<FluidVariant>} (droplets). Executes on outer commit. */
public final class FluidHandlerStorage implements Storage<FluidVariant> {

    private final IFluidHandler handler;

    public FluidHandlerStorage(IFluidHandler handler) {
        this.handler = handler;
    }

    public IFluidHandler handler() {
        return handler;
    }

    static FluidStack toStack(FluidVariant variant, int mb) {
        FluidStack stack = new FluidStack(variant.getRegistryEntry(), mb, variant.getComponents());
        return stack;
    }

    static FluidVariant toVariant(FluidStack stack) {
        return FluidVariant.of(stack.getFluid(), stack.getComponentsPatch());
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        int mb = TransferUtil.dropletsToMb(maxAmount);
        if (resource.isBlank() || mb <= 0) {
            return 0;
        }
        FluidStack stack = toStack(resource, mb);
        int filled = handler.fill(stack, IFluidHandler.FluidAction.SIMULATE);
        if (filled > 0) {
            FluidStack toFill = stack.copyWithAmount(filled);
            transaction.addOuterCloseCallback(result -> {
                if (result.wasCommitted()) {
                    handler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                }
            });
        }
        return TransferUtil.mbToDroplets(filled);
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        int mb = TransferUtil.dropletsToMb(maxAmount);
        if (resource.isBlank() || mb <= 0) {
            return 0;
        }
        FluidStack drained = handler.drain(toStack(resource, mb), IFluidHandler.FluidAction.SIMULATE);
        if (drained.isEmpty()) {
            return 0;
        }
        FluidStack toDrain = drained.copy();
        transaction.addOuterCloseCallback(result -> {
            if (result.wasCommitted()) {
                handler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
            }
        });
        return TransferUtil.mbToDroplets(drained.getAmount());
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        List<StorageView<FluidVariant>> views = new ArrayList<>();
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            int t = tank;
            views.add(new StorageView<>() {
                @Override
                public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
                    FluidStack inTank = handler.getFluidInTank(t);
                    if (inTank.isEmpty() || !toVariant(inTank).equals(resource)) {
                        return 0;
                    }
                    return FluidHandlerStorage.this.extract(resource, maxAmount, transaction);
                }

                @Override
                public boolean isResourceBlank() {
                    return handler.getFluidInTank(t).isEmpty();
                }

                @Override
                public FluidVariant getResource() {
                    FluidStack stack = handler.getFluidInTank(t);
                    return stack.isEmpty() ? FluidVariant.blank() : toVariant(stack);
                }

                @Override
                public long getAmount() {
                    return TransferUtil.mbToDroplets(handler.getFluidInTank(t).getAmount());
                }

                @Override
                public long getCapacity() {
                    return TransferUtil.mbToDroplets(handler.getTankCapacity(t));
                }
            });
        }
        return views.iterator();
    }
}
