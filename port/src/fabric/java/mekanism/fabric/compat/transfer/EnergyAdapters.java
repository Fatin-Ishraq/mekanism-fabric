package mekanism.fabric.compat.transfer;

import mekanism.neoshim.neoforge.transfer.energy.EnergyHandler;
import mekanism.neoshim.neoforge.transfer.transaction.Transaction;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;
import team.reborn.energy.api.EnergyStorage;

/** Energy handler &lt;-&gt; Team Reborn Energy, 1 FE = 1 E, same transactions. */
public final class EnergyAdapters {

    private EnergyAdapters() {
    }

    /** A Mekanism-side energy handler exposed as a TR energy storage. */
    public record ToTeamReborn(EnergyHandler handler) implements EnergyStorage {

        @Override
        public long insert(long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
            return handler.insert((int) Math.min(Integer.MAX_VALUE, maxAmount), Transaction.wrap(transaction));
        }

        @Override
        public long extract(long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
            return handler.extract((int) Math.min(Integer.MAX_VALUE, maxAmount), Transaction.wrap(transaction));
        }

        @Override
        public long getAmount() {
            return handler.getAmountAsLong();
        }

        @Override
        public long getCapacity() {
            return handler.getCapacityAsLong();
        }
    }

    /** A foreign TR energy storage viewed as an energy handler. */
    public record FromTeamReborn(EnergyStorage storage) implements EnergyHandler {

        @Override
        public long getAmountAsLong() {
            return storage.getAmount();
        }

        @Override
        public long getCapacityAsLong() {
            return storage.getCapacity();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            return (int) storage.insert(amount, Transaction.fabric(transaction));
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            return (int) storage.extract(amount, Transaction.fabric(transaction));
        }
    }
}
