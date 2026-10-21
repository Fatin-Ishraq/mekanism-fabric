package mekanism.neoshim.neoforge.transfer.transaction;

/**
 * A transaction a resource operation runs in. On Fabric every context is backed by a Fabric Transfer API transaction (see {@link Transaction}), so NeoForge-style
 * handlers and Fabric storages take part in the same transactions.
 */
public interface TransactionContext {

    int depth();
}
