package mekanism.fabric.compat.transfer;

import mekanism.neoshim.neoforge.transfer.access.ItemAccess;
import mekanism.neoshim.neoforge.transfer.item.ItemResource;
import mekanism.neoshim.neoforge.transfer.transaction.Transaction;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;

/** A Fabric {@link ContainerItemContext} viewed as an {@link ItemAccess}; both use the same transactions. */
public record FabricItemAccess(ContainerItemContext context) implements ItemAccess {

    @Override
    public ItemResource getResource() {
        return ItemResource.of(context.getItemVariant());
    }

    @Override
    public int getAmount() {
        return (int) Math.min(Integer.MAX_VALUE, context.getAmount());
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        return (int) context.insert(resource.toVariant(), amount, Transaction.fabric(transaction));
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return (int) context.extract(resource.toVariant(), amount, Transaction.fabric(transaction));
    }

    @Override
    public int exchange(ItemResource newResource, int amount, TransactionContext transaction) {
        return (int) context.exchange(newResource.toVariant(), amount, Transaction.fabric(transaction));
    }
}
