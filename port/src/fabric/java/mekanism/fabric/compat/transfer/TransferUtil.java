package mekanism.fabric.compat.transfer;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/** Helpers for running simulate/execute style operations against Fabric's transactional storages. */
public final class TransferUtil {

    /** 1 mB = 81 droplets. */
    public static final long DROPLETS_PER_MB = 81;

    private TransferUtil() {
    }

    /** Opens a nested transaction if one is already open on this thread, otherwise an outer one. */
    public static Transaction open() {
        TransactionContext current = Transaction.getCurrentUnsafe();
        return current != null ? current.openNested() : Transaction.openOuter();
    }

    public static long mbToDroplets(int mb) {
        return mb * DROPLETS_PER_MB;
    }

    public static int dropletsToMb(long droplets) {
        return (int) Math.min(Integer.MAX_VALUE, droplets / DROPLETS_PER_MB);
    }
}
