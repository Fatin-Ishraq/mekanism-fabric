package mekanism.neoshim.neoforge.transfer.energy;

import mekanism.neoshim.neoforge.transfer.transaction.Transaction;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;

public final class EnergyHandlerUtil {

    private EnergyHandlerUtil() {
    }

    public static boolean isFull(EnergyHandler handler) {
        return handler.getAmountAsLong() >= handler.getCapacityAsLong();
    }

    public static int getRedstoneSignalFromEnergyHandler(EnergyHandler handler) {
        long amount = handler.getAmountAsLong();
        if (amount <= 0) {
            return 0;
        }
        long capacity = handler.getCapacityAsLong();
        return capacity <= 0 ? 15 : 1 + (int) ((double) amount / capacity * 14);
    }

    public static int move(EnergyHandler from, EnergyHandler to, int amount, TransactionContext transaction) {
        try (Transaction simulation = Transaction.open(transaction)) {
            int extracted = from.extract(amount, simulation);
            int accepted = to.insert(extracted, simulation);
            amount = Math.min(extracted, accepted);
        }
        if (amount <= 0) {
            return 0;
        }
        try (Transaction nested = Transaction.open(transaction)) {
            int extracted = from.extract(amount, nested);
            int inserted = to.insert(extracted, nested);
            if (extracted == inserted) {
                nested.commit();
                return inserted;
            }
        }
        return 0;
    }
}
