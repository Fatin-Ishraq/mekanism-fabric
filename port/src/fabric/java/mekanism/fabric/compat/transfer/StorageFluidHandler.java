package mekanism.fabric.compat.transfer;

import java.util.ArrayList;
import java.util.List;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.fluids.capability.IFluidHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

/** Wraps a foreign Fabric {@code Storage<FluidVariant>} as an {@link IFluidHandler} (droplets -> mB, rounding down). */
public final class StorageFluidHandler implements IFluidHandler {

    private final Storage<FluidVariant> storage;

    public StorageFluidHandler(Storage<FluidVariant> storage) {
        this.storage = storage;
    }

    private List<StorageView<FluidVariant>> views() {
        List<StorageView<FluidVariant>> views = new ArrayList<>();
        for (StorageView<FluidVariant> view : storage) {
            views.add(view);
        }
        return views;
    }

    @Override
    public int getTanks() {
        return Math.max(1, views().size());
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        List<StorageView<FluidVariant>> views = views();
        if (tank >= views.size() || views.get(tank).isResourceBlank()) {
            return FluidStack.EMPTY;
        }
        StorageView<FluidVariant> view = views.get(tank);
        return FluidHandlerStorage.toStack(view.getResource(), TransferUtil.dropletsToMb(view.getAmount()));
    }

    @Override
    public int getTankCapacity(int tank) {
        List<StorageView<FluidVariant>> views = views();
        return tank < views.size() ? TransferUtil.dropletsToMb(views.get(tank).getCapacity()) : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        try (Transaction transaction = TransferUtil.open()) {
            long inserted = storage.insert(FluidHandlerStorage.toVariant(resource), TransferUtil.mbToDroplets(resource.getAmount()), transaction);
            //Only accept whole mB
            long whole = inserted - inserted % TransferUtil.DROPLETS_PER_MB;
            if (whole != inserted) {
                return 0;
            }
            if (action.execute()) {
                transaction.commit();
            }
            return TransferUtil.dropletsToMb(inserted);
        }
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        try (Transaction transaction = TransferUtil.open()) {
            long extracted = storage.extract(FluidHandlerStorage.toVariant(resource), TransferUtil.mbToDroplets(resource.getAmount()), transaction);
            if (extracted % TransferUtil.DROPLETS_PER_MB != 0 || extracted == 0) {
                return FluidStack.EMPTY;
            }
            if (action.execute()) {
                transaction.commit();
            }
            return resource.copyWithAmount(TransferUtil.dropletsToMb(extracted));
        }
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        for (StorageView<FluidVariant> view : views()) {
            if (!view.isResourceBlank()) {
                return drain(FluidHandlerStorage.toStack(view.getResource(), maxDrain), action);
            }
        }
        return FluidStack.EMPTY;
    }
}
