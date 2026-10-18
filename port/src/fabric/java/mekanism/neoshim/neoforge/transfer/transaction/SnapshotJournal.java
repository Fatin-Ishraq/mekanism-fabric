package mekanism.neoshim.neoforge.transfer.transaction;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext.CloseCallback;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext.Result;

/**
 * NeoForge-style journal of state snapshots: call {@link #updateSnapshots} before changing state inside a transaction; aborted transactions revert to the
 * snapshot, committed nested transactions pass it to the parent, and a committed root transaction calls {@link #onRootCommit} with the state before it.
 * Implemented on Fabric transaction close callbacks.
 */
public abstract class SnapshotJournal<T> {

    /** Snapshot wrapper (snapshots may legitimately be null, e.g. {@code SnapshotJournal<Void>}). */
    private record Entry<T>(T snapshot) {
    }

    private final List<Entry<T>> snapshots = new ArrayList<>();
    private final CloseCallback closeCallback = this::onClose;

    protected abstract T createSnapshot();

    protected abstract void revertToSnapshot(T snapshot);

    protected void releaseSnapshot(T snapshot) {
    }

    protected void onRootCommit(T originalState) {
    }

    public void updateSnapshots(TransactionContext transaction) {
        net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext fabric = Transaction.fabric(transaction);
        int depth = fabric.nestingDepth();
        while (snapshots.size() <= depth) {
            snapshots.add(null);
        }
        if (snapshots.get(depth) == null) {
            snapshots.set(depth, new Entry<>(createSnapshot()));
            fabric.addCloseCallback(closeCallback);
        }
    }

    private void onClose(net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction, Result result) {
        int depth = transaction.nestingDepth();
        Entry<T> entry = snapshots.set(depth, null);
        if (entry == null) {
            return;
        }
        if (result.wasAborted()) {
            revertToSnapshot(entry.snapshot());
            releaseSnapshot(entry.snapshot());
        } else if (depth > 0) {
            if (snapshots.get(depth - 1) == null) {
                //The parent had no snapshot yet: the state before this transaction is also the state before the parent
                snapshots.set(depth - 1, entry);
                transaction.getOpenTransaction(depth - 1).addCloseCallback(closeCallback);
            } else {
                releaseSnapshot(entry.snapshot());
            }
        } else {
            transaction.addOuterCloseCallback(outerResult -> {
                onRootCommit(entry.snapshot());
                releaseSnapshot(entry.snapshot());
            });
        }
    }
}
