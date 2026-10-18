package mekanism.fabric.compat.transfer;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;

/**
 * A one-slot item storage around a stack held by NeoForge-style code, used to give foreign Fabric item storages a {@link ContainerItemContext}. After an
 * operation the result is written back into the original stack when the item stayed the same (NeoForge item handlers mutate the stack in place); otherwise the
 * replacement is exposed as the new container (IFluidHandlerItem#getContainer).
 */
public final class ItemStackSlot extends SingleStackStorage {

    private ItemStack container;
    private ItemStack current;

    public ItemStackSlot(ItemStack container) {
        this.container = container;
        this.current = container;
    }

    public ContainerItemContext context() {
        return ContainerItemContext.ofSingleSlot(this);
    }

    @Override
    protected ItemStack getStack() {
        return current;
    }

    @Override
    protected void setStack(ItemStack stack) {
        current = stack;
    }

    /** Applies the committed result to the container stack (in place when the item did not change). */
    public void writeBack() {
        if (current == container) {
            return;
        }
        if (!container.isEmpty() && !current.isEmpty() && current.getItem() == container.getItem()) {
            container.setCount(current.getCount());
            ((PatchedDataComponentMap) container.getComponents()).restorePatch(current.getComponentsPatch());
            current = container;
        } else {
            container = current;
        }
    }

    public ItemStack container() {
        return container;
    }
}
