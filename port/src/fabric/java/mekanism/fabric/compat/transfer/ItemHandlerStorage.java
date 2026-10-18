package mekanism.fabric.compat.transfer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mekanism.neoshim.neoforge.items.IItemHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

/**
 * Exposes a NeoForge-style {@link IItemHandler} as a Fabric {@code Storage<ItemVariant>}. Operations are simulated immediately and executed
 * when the outermost transaction commits. Limitation: several operations on the same slot within one transaction each see the unmodified
 * slot (NOTES.md "Known bugs").
 */
public final class ItemHandlerStorage implements SlottedStorage<ItemVariant> {

    private final IItemHandler handler;

    public ItemHandlerStorage(IItemHandler handler) {
        this.handler = handler;
    }

    public IItemHandler handler() {
        return handler;
    }

    @Override
    public int getSlotCount() {
        return handler.getSlots();
    }

    @Override
    public SingleSlotStorage<ItemVariant> getSlot(int slot) {
        return new Slot(slot);
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        long inserted = 0;
        for (int slot = 0; slot < handler.getSlots() && inserted < maxAmount; slot++) {
            inserted += getSlot(slot).insert(resource, maxAmount - inserted, transaction);
        }
        return inserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        long extracted = 0;
        for (int slot = 0; slot < handler.getSlots() && extracted < maxAmount; slot++) {
            extracted += getSlot(slot).extract(resource, maxAmount - extracted, transaction);
        }
        return extracted;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        List<StorageView<ItemVariant>> views = new ArrayList<>(handler.getSlots());
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            views.add(new Slot(slot));
        }
        return views.iterator();
    }

    private final class Slot implements SingleSlotStorage<ItemVariant> {

        private final int slot;

        private Slot(int slot) {
            this.slot = slot;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0) {
                return 0;
            }
            ItemStack stack = resource.toStack((int) Math.min(maxAmount, resource.getItem().getDefaultMaxStackSize() * 64L));
            ItemStack remainder = handler.insertItem(slot, stack, true);
            int inserted = stack.getCount() - remainder.getCount();
            if (inserted > 0) {
                ItemStack toInsert = resource.toStack(inserted);
                transaction.addOuterCloseCallback(result -> {
                    if (result.wasCommitted()) {
                        handler.insertItem(slot, toInsert, false);
                    }
                });
            }
            return inserted;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0 || !resource.matches(handler.getStackInSlot(slot))) {
                return 0;
            }
            int amount = (int) Math.min(maxAmount, Integer.MAX_VALUE);
            ItemStack simulated = handler.extractItem(slot, amount, true);
            if (simulated.isEmpty() || !resource.matches(simulated)) {
                return 0;
            }
            int extracted = simulated.getCount();
            transaction.addOuterCloseCallback(result -> {
                if (result.wasCommitted()) {
                    handler.extractItem(slot, extracted, false);
                }
            });
            return extracted;
        }

        @Override
        public boolean isResourceBlank() {
            return handler.getStackInSlot(slot).isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(handler.getStackInSlot(slot));
        }

        @Override
        public long getAmount() {
            return handler.getStackInSlot(slot).getCount();
        }

        @Override
        public long getCapacity() {
            return handler.getSlotLimit(slot);
        }
    }
}
