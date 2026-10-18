package mekanism.fabric.compat.transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import mekanism.neoshim.neoforge.transfer.ResourceHandler;
import mekanism.neoshim.neoforge.transfer.TransferPreconditions;
import mekanism.neoshim.neoforge.transfer.resource.Resource;
import mekanism.neoshim.neoforge.transfer.transaction.Transaction;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;

/**
 * A foreign Fabric storage viewed as a NeoForge-style resource handler (same transactions). Slotted storages map index to slot; other storages expose their
 * current views as indices. {@code scale} is Fabric units per handler unit; Fabric amounts that are not whole handler units are rounded down.
 */
public class StorageResourceHandler<R extends Resource, V extends TransferVariant<?>> implements ResourceHandler<R> {

    private final Storage<V> storage;
    private final Function<V, R> toResource;
    private final Function<R, V> toVariant;
    private final long scale;

    public StorageResourceHandler(Storage<V> storage, Function<V, R> toResource, Function<R, V> toVariant, long scale) {
        this.storage = storage;
        this.toResource = toResource;
        this.toVariant = toVariant;
        this.scale = scale;
    }

    public Storage<V> storage() {
        return storage;
    }

    private List<? extends StorageView<V>> views() {
        if (storage instanceof SlottedStorage<V> slotted) {
            return slotted.getSlots();
        }
        List<StorageView<V>> views = new ArrayList<>();
        for (StorageView<V> view : storage) {
            views.add(view);
        }
        return views;
    }

    @Override
    public int size() {
        return views().size();
    }

    @Override
    public R getResource(int index) {
        List<? extends StorageView<V>> views = views();
        return index < views.size() ? toResource.apply(views.get(index).getResource()) : toResource.apply(null);
    }

    @Override
    public long getAmountAsLong(int index) {
        List<? extends StorageView<V>> views = views();
        return index < views.size() ? views.get(index).getAmount() / scale : 0;
    }

    @Override
    public long getCapacityAsLong(int index, R resource) {
        List<? extends StorageView<V>> views = views();
        return index < views.size() ? views.get(index).getCapacity() / scale : 0;
    }

    @Override
    public boolean isValid(int index, R resource) {
        return true;
    }

    @Override
    public int insert(int index, R resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        List<? extends StorageView<V>> views = views();
        if (index < views.size() && views.get(index) instanceof SingleSlotStorage<V> slot) {
            return (int) (slot.insert(toVariant.apply(resource), amount * scale, Transaction.fabric(transaction)) / scale);
        }
        //Not individually insertable: insert into the storage as a whole once, through the first index
        return index == 0 ? insert(resource, amount, transaction) : 0;
    }

    @Override
    public int insert(R resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return (int) (storage.insert(toVariant.apply(resource), amount * scale, Transaction.fabric(transaction)) / scale);
    }

    @Override
    public int extract(int index, R resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        List<? extends StorageView<V>> views = views();
        if (index >= views.size()) {
            return 0;
        }
        return (int) (views.get(index).extract(toVariant.apply(resource), amount * scale, Transaction.fabric(transaction)) / scale);
    }

    @Override
    public int extract(R resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return (int) (storage.extract(toVariant.apply(resource), amount * scale, Transaction.fabric(transaction)) / scale);
    }
}
