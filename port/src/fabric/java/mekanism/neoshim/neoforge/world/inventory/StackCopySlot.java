package mekanism.neoshim.neoforge.world.inventory;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A slot over storage that hands out copies: {@link #getItem()} returns a cached copy that menu code may mutate, and changes are written back on
 * {@link #setChanged()}.
 */
public abstract class StackCopySlot extends Slot {

    private static final SimpleContainer EMPTY_CONTAINER = new SimpleContainer(0);
    @Nullable
    private ItemStack cached;

    public StackCopySlot(int index, int x, int y) {
        super(EMPTY_CONTAINER, index, x, y);
    }

    protected abstract ItemStack getStackCopy();

    protected abstract void setStackCopy(ItemStack stack);

    @Override
    public final ItemStack getItem() {
        if (cached == null) {
            cached = getStackCopy();
        }
        return cached;
    }

    @Override
    public final void set(ItemStack stack) {
        setStackCopy(stack);
        cached = null;
        setChanged();
    }

    @Override
    public final void setChanged() {
        if (cached != null) {
            setStackCopy(cached);
            cached = null;
        }
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack removed = getItem().split(amount);
        setChanged();
        return removed;
    }

    protected void clearCachedReturnStack() {
        cached = null;
    }
}
