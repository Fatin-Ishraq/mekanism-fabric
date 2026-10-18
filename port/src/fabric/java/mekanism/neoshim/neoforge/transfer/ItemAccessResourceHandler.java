package mekanism.neoshim.neoforge.transfer;

import mekanism.neoshim.neoforge.transfer.access.ItemAccess;
import mekanism.neoshim.neoforge.transfer.item.ItemResource;
import mekanism.neoshim.neoforge.transfer.resource.Resource;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;

/**
 * A handler whose contents are stored on an item (read from and written back to an {@link ItemAccess}). Amounts scale with the number of items in the access:
 * every item holds the same contents, and changes exchange all of them.
 */
public abstract class ItemAccessResourceHandler<T extends Resource> implements ResourceHandler<T> {

    protected final ItemAccess itemAccess;
    protected final int size;

    protected ItemAccessResourceHandler(ItemAccess itemAccess, int size) {
        this.itemAccess = itemAccess;
        this.size = size;
    }

    protected abstract T getResourceFrom(ItemResource itemResource, int index);

    protected abstract int getAmountFrom(ItemResource itemResource, int index);

    protected abstract ItemResource update(ItemResource itemResource, int index, T newResource, int newAmount);

    @Override
    public boolean isValid(int index, T resource) {
        return true;
    }

    protected abstract int getCapacity(int index, T resource);

    @Override
    public int size() {
        return size;
    }

    @Override
    public T getResource(int index) {
        return getResourceFrom(itemAccess.getResource(), index);
    }

    @Override
    public long getAmountAsLong(int index) {
        return (long) getAmountFrom(itemAccess.getResource(), index) * itemAccess.getAmount();
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        return (long) getCapacity(index, resource) * itemAccess.getAmount();
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        ItemResource item = itemAccess.getResource();
        int count = itemAccess.getAmount();
        if (item.isEmpty() || count <= 0 || !isValid(index, resource)) {
            return 0;
        }
        T current = getResourceFrom(item, index);
        int currentAmount = getAmountFrom(item, index);
        if (currentAmount > 0 && !current.equals(resource)) {
            return 0;
        }
        int perItem = Math.min(amount / count, getCapacity(index, resource) - currentAmount);
        if (perItem <= 0) {
            return 0;
        }
        ItemResource updated = update(item, index, resource, currentAmount + perItem);
        if (updated.isEmpty()) {
            return 0;
        }
        return perItem * itemAccess.exchange(updated, count, transaction);
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        ItemResource item = itemAccess.getResource();
        int count = itemAccess.getAmount();
        if (item.isEmpty() || count <= 0) {
            return 0;
        }
        int currentAmount = getAmountFrom(item, index);
        if (currentAmount <= 0 || !getResourceFrom(item, index).equals(resource)) {
            return 0;
        }
        int perItem = Math.min(amount / count, currentAmount);
        if (perItem <= 0) {
            return 0;
        }
        ItemResource updated = update(item, index, resource, currentAmount - perItem);
        if (updated.isEmpty()) {
            return 0;
        }
        return perItem * itemAccess.exchange(updated, count, transaction);
    }
}
