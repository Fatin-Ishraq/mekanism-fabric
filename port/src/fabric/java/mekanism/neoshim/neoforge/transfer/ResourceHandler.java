package mekanism.neoshim.neoforge.transfer;

import mekanism.neoshim.neoforge.transfer.resource.Resource;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;

/**
 * An indexed container of resources supporting transactional insertion and extraction. Amounts are ints; for fluids they are millibuckets. Bridged to and from
 * Fabric's {@code Storage} by mekanism.fabric.compat.transfer.
 */
public interface ResourceHandler<T extends Resource> {

    int size();

    T getResource(int index);

    long getAmountAsLong(int index);

    default int getAmountAsInt(int index) {
        return (int) Math.min(Integer.MAX_VALUE, getAmountAsLong(index));
    }

    long getCapacityAsLong(int index, T resource);

    default int getCapacityAsInt(int index, T resource) {
        return (int) Math.min(Integer.MAX_VALUE, getCapacityAsLong(index, resource));
    }

    boolean isValid(int index, T resource);

    int insert(int index, T resource, int amount, TransactionContext transaction);

    /** Inserts into the indices in order. */
    default int insert(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int inserted = 0;
        for (int index = 0, size = size(); index < size && inserted < amount; index++) {
            inserted += insert(index, resource, amount - inserted, transaction);
        }
        return inserted;
    }

    int extract(int index, T resource, int amount, TransactionContext transaction);

    /** Extracts from the indices in order. */
    default int extract(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int extracted = 0;
        for (int index = 0, size = size(); index < size && extracted < amount; index++) {
            extracted += extract(index, resource, amount - extracted, transaction);
        }
        return extracted;
    }

    @SuppressWarnings("unchecked")
    static <T extends Resource> Class<ResourceHandler<T>> asClass() {
        return (Class<ResourceHandler<T>>) (Class<?>) ResourceHandler.class;
    }
}
