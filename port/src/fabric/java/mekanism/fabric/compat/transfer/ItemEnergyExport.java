package mekanism.fabric.compat.transfer;

import java.util.function.Function;
import mekanism.neoshim.neoforge.energy.IEnergyStorage;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

/**
 * A Mekanism item's energy capability exported as a Team Reborn Energy item storage: each change is applied to a copy of one item, which is then exchanged into
 * the context (Fabric's way of mutating items in place).
 */
public final class ItemEnergyExport implements EnergyStorage {

    private final ContainerItemContext context;
    private final Function<ItemStack, @Nullable IEnergyStorage> capability;

    public ItemEnergyExport(ContainerItemContext context, Function<ItemStack, @Nullable IEnergyStorage> capability) {
        this.context = context;
        this.capability = capability;
    }

    @Nullable
    private IEnergyStorage handler(ItemStack copy) {
        return copy.isEmpty() ? null : capability.apply(copy);
    }

    private ItemStack copy() {
        return context.getItemVariant().toStack(1);
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        ItemStack copy = copy();
        IEnergyStorage handler = handler(copy);
        if (handler == null) {
            return 0;
        }
        int inserted = handler.receiveEnergy((int) Math.min(Integer.MAX_VALUE, maxAmount), false);
        return inserted > 0 && context.exchange(ItemVariant.of(copy), 1, transaction) == 1 ? inserted : 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        ItemStack copy = copy();
        IEnergyStorage handler = handler(copy);
        if (handler == null) {
            return 0;
        }
        int extracted = handler.extractEnergy((int) Math.min(Integer.MAX_VALUE, maxAmount), false);
        return extracted > 0 && context.exchange(ItemVariant.of(copy), 1, transaction) == 1 ? extracted : 0;
    }

    @Override
    public long getAmount() {
        IEnergyStorage handler = handler(copy());
        return handler == null ? 0 : handler.getEnergyStored();
    }

    @Override
    public long getCapacity() {
        IEnergyStorage handler = handler(copy());
        return handler == null ? 0 : handler.getMaxEnergyStored();
    }

    @Override
    public boolean supportsInsertion() {
        IEnergyStorage handler = handler(copy());
        return handler != null && handler.canReceive();
    }

    @Override
    public boolean supportsExtraction() {
        IEnergyStorage handler = handler(copy());
        return handler != null && handler.canExtract();
    }
}
