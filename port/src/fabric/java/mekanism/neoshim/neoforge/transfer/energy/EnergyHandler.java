package mekanism.neoshim.neoforge.transfer.energy;

import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;

/** Transactional energy storage (Forge Energy units). Bridged to Team Reborn Energy's {@code EnergyStorage} by mekanism.fabric.compat.transfer. */
public interface EnergyHandler {

    long getAmountAsLong();

    default int getAmountAsInt() {
        return (int) Math.min(Integer.MAX_VALUE, getAmountAsLong());
    }

    long getCapacityAsLong();

    default int getCapacityAsInt() {
        return (int) Math.min(Integer.MAX_VALUE, getCapacityAsLong());
    }

    int insert(int amount, TransactionContext transaction);

    int extract(int amount, TransactionContext transaction);
}
