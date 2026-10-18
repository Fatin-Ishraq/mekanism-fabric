package mekanism.api.inventory.access;

import java.util.Objects;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.neoshim.neoforge.transfer.ResourceHandler;
import mekanism.neoshim.neoforge.transfer.access.ItemAccess;
import mekanism.neoshim.neoforge.transfer.item.ItemResource;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;

/// Item access that interacts with a given inventory slot using the given automation type.
///
/// @see ItemAccess#forHandlerIndexStrict(ResourceHandler, int)
/// @since 10.8.0
public class InventorySlotItemAccess implements ItemAccess {

    protected final IInventorySlot slot;
    protected final AutomationType automationType;

    /// Creates an item access instance for a specific inventory slot
    public InventorySlotItemAccess(IInventorySlot slot, AutomationType automationType) {
        this.slot = Objects.requireNonNull(slot, "Slot may not be null");
        this.automationType = Objects.requireNonNull(automationType, "Automation type may not be null");
    }

    @Override
    public ItemResource getResource() {
        return slot.resource();
    }

    @Override
    public int getAmount() {
        return slot.amountAsInt();
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        return slot.insert(resource, amount, transaction, automationType);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return slot.extract(resource, amount, transaction, automationType);
    }
}