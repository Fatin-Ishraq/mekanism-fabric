package mekanism.neoshim.neoforge.transfer.item;

import java.util.List;
import mekanism.fabric.compat.transfer.SlottedStorageResourceHandler;
import mekanism.neoshim.neoforge.transfer.ResourceHandler;
import mekanism.neoshim.neoforge.transfer.transaction.Transaction;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/** A player's inventory as a resource handler, backed by Fabric's {@link PlayerInventoryStorage} (indices are vanilla inventory slots). */
public final class PlayerInventoryWrapper extends SlottedStorageResourceHandler {

    private final PlayerInventoryStorage storage;
    private final Inventory inventory;

    private PlayerInventoryWrapper(Inventory inventory) {
        super(PlayerInventoryStorage.of(inventory));
        this.storage = PlayerInventoryStorage.of(inventory);
        this.inventory = inventory;
    }

    public static PlayerInventoryWrapper of(Player player) {
        return of(player.getInventory());
    }

    public static PlayerInventoryWrapper of(Inventory inventory) {
        return new PlayerInventoryWrapper(inventory);
    }

    public ResourceHandler<ItemResource> getSlot(int index) {
        return new SlottedStorageResourceHandler(List.of(storage.getSlot(index)));
    }

    public ResourceHandler<ItemResource> getMainHandSlot() {
        return getSlot(inventory.getSelectedSlot());
    }

    public ResourceHandler<ItemResource> getHandSlot(InteractionHand hand) {
        return new SlottedStorageResourceHandler(List.of(storage.getHandSlot(hand)));
    }

    public ResourceHandler<ItemResource> getHandSlots() {
        return new SlottedStorageResourceHandler(List.of(storage.getHandSlot(InteractionHand.MAIN_HAND), storage.getHandSlot(InteractionHand.OFF_HAND)));
    }

    public ResourceHandler<ItemResource> getMainSlots() {
        return new SlottedStorageResourceHandler(storage.getSlots().subList(0, Inventory.INVENTORY_SIZE));
    }

    public ResourceHandler<ItemResource> getArmorSlot(EquipmentSlot slot) {
        return new SlottedStorageResourceHandler(List.of(storage.getSlot(Inventory.INVENTORY_SIZE + slot.getIndex())));
    }

    public ResourceHandler<ItemResource> getArmorSlots() {
        List<SingleSlotStorage<ItemVariant>> slots = storage.getSlots();
        return new SlottedStorageResourceHandler(slots.subList(Inventory.INVENTORY_SIZE, Inventory.INVENTORY_SIZE + 4));
    }

    /** Inserts like a player picking items up: hand and matching stacks first, then empty slots. */
    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        return (int) storage.insert(resource.toVariant(), amount, Transaction.fabric(transaction));
    }

    public void placeItemBackInInventory(ItemResource resource, int amount, TransactionContext transaction) {
        storage.offerOrDrop(resource.toVariant(), amount, Transaction.fabric(transaction));
    }

    public void drop(ItemResource resource, int amount, boolean throwRandomly, boolean retainOwnership, TransactionContext transaction) {
        storage.drop(resource.toVariant(), amount, throwRandomly, retainOwnership, Transaction.fabric(transaction));
    }

    @Override
    public String toString() {
        return "PlayerInventoryWrapper[" + inventory.player.getName().getString() + "]";
    }
}
