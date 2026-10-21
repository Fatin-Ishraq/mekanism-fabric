package mekanism.neoshim.neoforge.transfer.transaction;

import java.util.Objects;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge-style transaction backed by a Fabric Transfer API transaction. Opening a root opens a Fabric outer transaction, nested transactions nest in Fabric, so a
 * Mekanism handler called from a Fabric storage operation (or the reverse) shares one transaction stack.
 */
public final class Transaction implements AutoCloseable, TransactionContext {

    public enum Lifecycle {
        NONE,
        OPEN,
        CLOSING,
        ROOT_CLOSING
    }

    private final net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext fabric;
    @Nullable
    private final net.fabricmc.fabric.api.transfer.v1.transaction.Transaction owned;

    private Transaction(net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext fabric,
          @Nullable net.fabricmc.fabric.api.transfer.v1.transaction.Transaction owned) {
        this.fabric = fabric;
        this.owned = owned;
    }

    public static Transaction openRoot() {
        net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter();
        return new Transaction(transaction, transaction);
    }

    /** Opens a transaction nested in {@code parent}, or a root transaction if there is no parent. */
    public static Transaction open(@Nullable TransactionContext parent) {
        if (parent == null) {
            return openRoot();
        }
        net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = fabric(parent).openNested();
        return new Transaction(transaction, transaction);
    }

    public static Lifecycle getLifecycle() {
        return switch (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.getLifecycle()) {
            case NONE -> Lifecycle.NONE;
            case OPEN -> Lifecycle.OPEN;
            case CLOSING -> Lifecycle.CLOSING;
            case OUTER_CLOSING -> Lifecycle.ROOT_CLOSING;
        };
    }

    @Nullable
    public static TransactionContext getCurrentOpenedTransaction() {
        net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext current = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.getCurrentUnsafe();
        return current == null ? null : wrap(current);
    }

    /** Views a Fabric transaction as a NeoForge-style context (the caller keeps ownership: commit/close on the wrapper do nothing). */
    public static TransactionContext wrap(net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext fabric) {
        return new Transaction(Objects.requireNonNull(fabric), null);
    }

    /** The Fabric transaction behind a context created by this class. */
    public static net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext fabric(TransactionContext context) {
        if (context instanceof Transaction transaction) {
            return transaction.fabric;
        }
        throw new IllegalArgumentException("Transaction context not created by mekanism.neoshim Transaction: " + context);
    }

    public void commit() {
        if (owned == null) {
            throw new IllegalStateException("Cannot commit a transaction that was not opened through this wrapper");
        }
        owned.commit();
    }

    @Override
    public void close() {
        if (owned != null) {
            owned.close();
        }
    }

    @Override
    public int depth() {
        return fabric.nestingDepth();
    }

    @Override
    public String toString() {
        return "Transaction[depth=" + depth() + "]";
    }
}
