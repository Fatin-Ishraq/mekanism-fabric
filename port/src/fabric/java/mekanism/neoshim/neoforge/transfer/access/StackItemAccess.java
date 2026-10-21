package mekanism.neoshim.neoforge.transfer.access;

import mekanism.neoshim.neoforge.transfer.item.ItemResource;
import mekanism.neoshim.neoforge.transfer.transaction.SnapshotJournal;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;

/** Modifies one stack in place (count and components; the item type cannot change). */
final class StackItemAccess extends SnapshotJournal<StackItemAccess.State> implements ItemAccess {

    record State(int count, DataComponentPatch patch) {
    }

    private final ItemStack stack;

    StackItemAccess(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemResource getResource() {
        return ItemResource.of(stack);
    }

    @Override
    public int getAmount() {
        return stack.getCount();
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (stack.isEmpty() || !resource.matches(stack)) {
            return 0;
        }
        int inserted = Math.min(amount, stack.getMaxStackSize() - stack.getCount());
        if (inserted > 0) {
            updateSnapshots(transaction);
            stack.grow(inserted);
        }
        return Math.max(inserted, 0);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        if (stack.isEmpty() || !resource.matches(stack)) {
            return 0;
        }
        int extracted = Math.min(amount, stack.getCount());
        if (extracted > 0) {
            updateSnapshots(transaction);
            stack.shrink(extracted);
        }
        return extracted;
    }

    @Override
    public int exchange(ItemResource newResource, int amount, TransactionContext transaction) {
        //Only whole-stack component changes of the same item are possible in place
        if (stack.isEmpty() || amount != stack.getCount() || newResource.isEmpty() || newResource.getItem() != stack.getItem()) {
            return 0;
        }
        updateSnapshots(transaction);
        ((PatchedDataComponentMap) stack.getComponents()).restorePatch(newResource.getComponentsPatch());
        return amount;
    }

    @Override
    protected State createSnapshot() {
        return new State(stack.getCount(), stack.getComponentsPatch());
    }

    @Override
    protected void revertToSnapshot(State snapshot) {
        stack.setCount(snapshot.count());
        if (!stack.isEmpty()) {
            ((PatchedDataComponentMap) stack.getComponents()).restorePatch(snapshot.patch());
        }
    }
}
