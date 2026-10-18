package mekanism.fabric.compat.transfer;

import java.util.function.Function;
import mekanism.neoshim.neoforge.transfer.ResourceHandler;
import mekanism.neoshim.neoforge.transfer.resource.Resource;
import mekanism.neoshim.neoforge.transfer.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * A NeoForge-style resource handler exposed as a Fabric slotted storage. Both run in the same transactions; {@code scale} converts handler amounts to Fabric
 * amounts (1 for items, 81 droplets per millibucket for fluids). Fabric amounts that are not whole handler units are rounded down.
 */
public class ResourceHandlerStorage<R extends Resource, V extends TransferVariant<?>> implements SlottedStorage<V> {

    private final ResourceHandler<R> handler;
    private final Function<V, R> toResource;
    private final Function<R, V> toVariant;
    private final long scale;

    public ResourceHandlerStorage(ResourceHandler<R> handler, Function<V, R> toResource, Function<R, V> toVariant, long scale) {
        this.handler = handler;
        this.toResource = toResource;
        this.toVariant = toVariant;
        this.scale = scale;
    }

    public ResourceHandler<R> handler() {
        return handler;
    }

    private int toHandlerAmount(long amount) {
        return (int) Math.min(Integer.MAX_VALUE, amount / scale);
    }

    @Override
    public long insert(V resource, long maxAmount, TransactionContext transaction) {
        int amount = toHandlerAmount(maxAmount);
        if (resource.isBlank() || amount <= 0) {
            return 0;
        }
        return handler.insert(toResource.apply(resource), amount, Transaction.wrap(transaction)) * scale;
    }

    @Override
    public long extract(V resource, long maxAmount, TransactionContext transaction) {
        int amount = toHandlerAmount(maxAmount);
        if (resource.isBlank() || amount <= 0) {
            return 0;
        }
        return handler.extract(toResource.apply(resource), amount, Transaction.wrap(transaction)) * scale;
    }

    @Override
    public int getSlotCount() {
        return handler.size();
    }

    @Override
    public SingleSlotStorage<V> getSlot(int index) {
        return new IndexSlot(index);
    }

    @Override
    public java.util.Iterator<net.fabricmc.fabric.api.transfer.v1.storage.StorageView<V>> iterator() {
        java.util.List<net.fabricmc.fabric.api.transfer.v1.storage.StorageView<V>> views = new java.util.ArrayList<>(handler.size());
        for (int index = 0; index < handler.size(); index++) {
            views.add(new IndexSlot(index));
        }
        return views.iterator();
    }

    private final class IndexSlot implements SingleSlotStorage<V> {

        private final int index;

        private IndexSlot(int index) {
            this.index = index;
        }

        @Override
        public long insert(V resource, long maxAmount, TransactionContext transaction) {
            int amount = toHandlerAmount(maxAmount);
            if (resource.isBlank() || amount <= 0) {
                return 0;
            }
            return handler.insert(index, toResource.apply(resource), amount, Transaction.wrap(transaction)) * scale;
        }

        @Override
        public long extract(V resource, long maxAmount, TransactionContext transaction) {
            int amount = toHandlerAmount(maxAmount);
            if (resource.isBlank() || amount <= 0) {
                return 0;
            }
            return handler.extract(index, toResource.apply(resource), amount, Transaction.wrap(transaction)) * scale;
        }

        @Override
        public boolean isResourceBlank() {
            return handler.getResource(index).isEmpty();
        }

        @Override
        public V getResource() {
            return toVariant.apply(handler.getResource(index));
        }

        @Override
        public long getAmount() {
            return handler.getAmountAsLong(index) * scale;
        }

        @Override
        public long getCapacity() {
            return handler.getCapacityAsLong(index, handler.getResource(index)) * scale;
        }
    }
}
