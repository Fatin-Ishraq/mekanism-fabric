package mekanism.neoshim.neoforge.transfer.access;

import mekanism.fabric.compat.transfer.FabricItemAccess;
import mekanism.neoshim.neoforge.capabilities.ItemCapability;
import mekanism.neoshim.neoforge.transfer.ResourceHandler;
import mekanism.neoshim.neoforge.transfer.item.ItemResource;
import mekanism.neoshim.neoforge.transfer.transaction.Transaction;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Where an item that is being used as a container lives (a hand, a slot, a cursor...), so that capabilities can replace or change it. Fabric equivalent:
 * {@link ContainerItemContext}, which backs the player-related accesses.
 */
public interface ItemAccess {

    static ItemAccess forPlayerInteraction(Player player, InteractionHand hand) {
        return new FabricItemAccess(ContainerItemContext.forPlayerInteraction(player, hand));
    }

    static ItemAccess forInfiniteMaterials(Player player, ItemStack stack) {
        return new FabricItemAccess(ContainerItemContext.forCreativeInteraction(player, stack));
    }

    static ItemAccess forPlayerCursor(Player player, AbstractContainerMenu menu) {
        return new FabricItemAccess(ContainerItemContext.ofPlayerCursor(player, menu));
    }

    static ItemAccess forPlayerSlot(Player player, int slot) {
        return new FabricItemAccess(ContainerItemContext.ofPlayerSlot(player, PlayerInventoryStorage.of(player).getSlot(slot)));
    }

    /** The item at one index of a handler; overflow from exchanges goes to the rest of the handler. */
    static ItemAccess forHandlerIndex(ResourceHandler<ItemResource> handler, int index) {
        return new HandlerItemAccess(handler, index, false);
    }

    /** The item at one index of a handler; nothing can overflow out of that index. */
    static ItemAccess forHandlerIndexStrict(ResourceHandler<ItemResource> handler, int index) {
        return new HandlerItemAccess(handler, index, true);
    }

    /** Modifies the given stack in place; operations that would change the item type fail. */
    static ItemAccess forStack(ItemStack stack) {
        return new StackItemAccess(stack);
    }

    /** A view exposing at most one of the items. */
    default ItemAccess oneByOne() {
        return new OneByOneItemAccess(this);
    }

    @Nullable
    default <T> T getCapability(ItemCapability<T, ItemAccess> capability) {
        ItemResource resource = getResource();
        if (resource.isEmpty() || getAmount() <= 0) {
            return null;
        }
        return capability.getCapability(resource.toStack(getAmount()), this);
    }

    ItemResource getResource();

    int getAmount();

    int insert(ItemResource resource, int amount, TransactionContext transaction);

    int extract(ItemResource resource, int amount, TransactionContext transaction);

    /** Replaces {@code amount} of the current item with {@code amount} of {@code newResource}; all or nothing. */
    default int exchange(ItemResource newResource, int amount, TransactionContext transaction) {
        try (Transaction nested = Transaction.open(transaction)) {
            ItemResource current = getResource();
            if (current.isEmpty() || extract(current, amount, nested) != amount) {
                return 0;
            }
            if (!newResource.isEmpty() && insert(newResource, amount, nested) != amount) {
                return 0;
            }
            nested.commit();
            return amount;
        }
    }
}
