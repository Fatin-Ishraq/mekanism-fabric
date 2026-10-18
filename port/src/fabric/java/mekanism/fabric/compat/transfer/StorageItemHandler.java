package mekanism.fabric.compat.transfer;

import java.util.ArrayList;
import java.util.List;
import mekanism.neoshim.neoforge.items.IItemHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

/** Wraps a foreign Fabric {@code Storage<ItemVariant>} (e.g. a vanilla chest) as an {@link IItemHandler}. */
public final class StorageItemHandler implements IItemHandler {

    private final Storage<ItemVariant> storage;

    public StorageItemHandler(Storage<ItemVariant> storage) {
        this.storage = storage;
    }

    private List<StorageView<ItemVariant>> views() {
        List<StorageView<ItemVariant>> views = new ArrayList<>();
        for (StorageView<ItemVariant> view : storage) {
            views.add(view);
        }
        return views;
    }

    @Override
    public int getSlots() {
        if (storage instanceof SlottedStorage<ItemVariant> slotted) {
            return slotted.getSlotCount();
        }
        //Non-slotted storage: expose its current views plus one "virtual" insertion slot
        return views().size() + 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        StorageView<ItemVariant> view = view(slot);
        if (view == null || view.isResourceBlank()) {
            return ItemStack.EMPTY;
        }
        return view.getResource().toStack((int) Math.min(Integer.MAX_VALUE, view.getAmount()));
    }

    private StorageView<ItemVariant> view(int slot) {
        if (storage instanceof SlottedStorage<ItemVariant> slotted) {
            return slot < slotted.getSlotCount() ? slotted.getSlot(slot) : null;
        }
        List<StorageView<ItemVariant>> views = views();
        return slot < views.size() ? views.get(slot) : null;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return stack;
        }
        try (Transaction transaction = TransferUtil.open()) {
            long inserted;
            if (storage instanceof SlottedStorage<ItemVariant> slotted) {
                if (slot >= slotted.getSlotCount()) {
                    return stack;
                }
                inserted = slotted.getSlot(slot).insert(ItemVariant.of(stack), stack.getCount(), transaction);
            } else {
                inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            }
            if (!simulate) {
                transaction.commit();
            }
            return inserted == 0 ? stack : stack.copyWithCount(stack.getCount() - (int) inserted);
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        StorageView<ItemVariant> view = view(slot);
        if (amount <= 0 || view == null || view.isResourceBlank()) {
            return ItemStack.EMPTY;
        }
        ItemVariant resource = view.getResource();
        try (Transaction transaction = TransferUtil.open()) {
            long extracted = view.extract(resource, amount, transaction);
            if (!simulate) {
                transaction.commit();
            }
            return extracted == 0 ? ItemStack.EMPTY : resource.toStack((int) extracted);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        StorageView<ItemVariant> view = view(slot);
        return view == null ? 64 : (int) Math.min(Integer.MAX_VALUE, view.getCapacity());
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        try (Transaction transaction = TransferUtil.open()) {
            return storage.insert(ItemVariant.of(stack), 1, transaction) > 0;
        }
    }
}
