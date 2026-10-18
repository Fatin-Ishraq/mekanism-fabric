package mekanism.neoshim.neoforge.transfer.access;

import mekanism.neoshim.neoforge.transfer.item.ItemResource;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;

record OneByOneItemAccess(ItemAccess delegate) implements ItemAccess {

    @Override
    public ItemResource getResource() {
        return delegate.getResource();
    }

    @Override
    public int getAmount() {
        return Math.min(1, delegate.getAmount());
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        return delegate.insert(resource, amount, transaction);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return delegate.extract(resource, Math.min(amount, getAmount()), transaction);
    }

    @Override
    public int exchange(ItemResource newResource, int amount, TransactionContext transaction) {
        return delegate.exchange(newResource, Math.min(amount, getAmount()), transaction);
    }

    @Override
    public ItemAccess oneByOne() {
        return this;
    }
}
