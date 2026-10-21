package mekanism.fabric.compat.transfer;

import java.util.List;
import mekanism.neoshim.neoforge.transfer.access.ItemAccess;
import mekanism.neoshim.neoforge.transfer.item.ItemResource;
import mekanism.neoshim.neoforge.transfer.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/** An {@link ItemAccess} viewed as a Fabric {@link ContainerItemContext}, for looking up foreign Fabric item storages from Mekanism code. */
public final class ItemAccessContext implements ContainerItemContext {

    private final ItemAccess access;
    private final SingleSlotStorage<ItemVariant> mainSlot = new SingleSlotStorage<>() {
        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return resource.isBlank() ? 0 : access.insert(ItemResource.of(resource), (int) Math.min(Integer.MAX_VALUE, maxAmount), Transaction.wrap(transaction));
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return resource.isBlank() ? 0 : access.extract(ItemResource.of(resource), (int) Math.min(Integer.MAX_VALUE, maxAmount), Transaction.wrap(transaction));
        }

        @Override
        public boolean isResourceBlank() {
            return access.getResource().isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            return access.getResource().toVariant();
        }

        @Override
        public long getAmount() {
            return access.getAmount();
        }

        @Override
        public long getCapacity() {
            ItemResource resource = access.getResource();
            return resource.isEmpty() ? 99 : resource.getMaxStackSize();
        }
    };

    private ItemAccessContext(ItemAccess access) {
        this.access = access;
    }

    /** Unwraps accesses that are already Fabric contexts. */
    public static ContainerItemContext of(ItemAccess access) {
        return access instanceof FabricItemAccess fabric ? fabric.context() : new ItemAccessContext(access);
    }

    @Override
    public SingleSlotStorage<ItemVariant> getMainSlot() {
        return mainSlot;
    }

    @Override
    public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
        return mainSlot.insert(itemVariant, maxAmount, transactionContext);
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
        return List.of();
    }
}
