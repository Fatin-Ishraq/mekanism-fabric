package mekanism.fabric.compat.transfer;

import java.util.List;
import mekanism.neoshim.neoforge.transfer.ResourceHandler;
import mekanism.neoshim.neoforge.transfer.TransferPreconditions;
import mekanism.neoshim.neoforge.transfer.item.ItemResource;
import mekanism.neoshim.neoforge.transfer.transaction.Transaction;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;

/** Fabric item slots viewed as an item resource handler (same transactions, same indices). */
public class SlottedStorageResourceHandler implements ResourceHandler<ItemResource> {

    private final List<SingleSlotStorage<ItemVariant>> slots;

    public SlottedStorageResourceHandler(SlottedStorage<ItemVariant> storage) {
        this(storage.getSlots());
    }

    public SlottedStorageResourceHandler(List<SingleSlotStorage<ItemVariant>> slots) {
        this.slots = slots;
    }

    @Override
    public int size() {
        return slots.size();
    }

    @Override
    public ItemResource getResource(int index) {
        return ItemResource.of(slots.get(index).getResource());
    }

    @Override
    public long getAmountAsLong(int index) {
        return slots.get(index).getAmount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return slots.get(index).getCapacity();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return true;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return (int) slots.get(index).insert(resource.toVariant(), amount, Transaction.fabric(transaction));
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return (int) slots.get(index).extract(resource.toVariant(), amount, Transaction.fabric(transaction));
    }
}
