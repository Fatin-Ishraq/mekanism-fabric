package mekanism.neoshim.neoforge.transfer.item;

import mekanism.neoshim.neoforge.transfer.ResourceHandler;
import mekanism.neoshim.neoforge.transfer.ResourceHandlerUtil;
import mekanism.neoshim.neoforge.transfer.transaction.Transaction;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ItemUtil {

    private ItemUtil() {
    }

    public static ItemStack getStack(ResourceHandler<ItemResource> handler, int index) {
        return handler.getResource(index).toStack(handler.getAmountAsInt(index));
    }

    public static ItemStack insertItemReturnRemaining(ResourceHandler<ItemResource> handler, ItemStack stack, boolean simulate,
          @Nullable TransactionContext transaction) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try (Transaction nested = Transaction.open(transaction)) {
            int inserted = ResourceHandlerUtil.insertStacking(handler, ItemResource.of(stack), stack.getCount(), nested);
            if (!simulate) {
                nested.commit();
            }
            return stack.copyWithCount(stack.getCount() - inserted);
        }
    }

    public static ItemStack insertItemReturnRemaining(ResourceHandler<ItemResource> handler, int index, ItemStack stack, boolean simulate,
          @Nullable TransactionContext transaction) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try (Transaction nested = Transaction.open(transaction)) {
            int inserted = handler.insert(index, ItemResource.of(stack), stack.getCount(), nested);
            if (!simulate) {
                nested.commit();
            }
            return stack.copyWithCount(stack.getCount() - inserted);
        }
    }
}
