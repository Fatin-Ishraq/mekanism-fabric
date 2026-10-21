package mekanism.neoshim.neoforge.transfer;

import java.util.function.Predicate;
import mekanism.neoshim.neoforge.transfer.resource.Resource;
import mekanism.neoshim.neoforge.transfer.resource.ResourceStack;
import mekanism.neoshim.neoforge.transfer.transaction.Transaction;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public final class ResourceHandlerUtil {

    private ResourceHandlerUtil() {
    }

    public static boolean isEmpty(Resource resource, int amount) {
        return resource.isEmpty() || amount <= 0;
    }

    public static boolean isEmpty(ResourceHandler<? extends Resource> handler) {
        for (int index = 0; index < handler.size(); index++) {
            if (!handler.getResource(index).isEmpty() && handler.getAmountAsLong(index) > 0) {
                return false;
            }
        }
        return true;
    }

    public static <T extends Resource> boolean isFull(ResourceHandler<T> handler) {
        for (int index = 0; index < handler.size(); index++) {
            T resource = handler.getResource(index);
            if (resource.isEmpty() || handler.getAmountAsLong(index) < handler.getCapacityAsLong(index, resource)) {
                return false;
            }
        }
        return true;
    }

    public static <T extends Resource> boolean isValid(ResourceHandler<T> handler, T resource) {
        for (int index = 0; index < handler.size(); index++) {
            if (handler.isValid(index, resource)) {
                return true;
            }
        }
        return false;
    }

    /** Comparator-style redstone signal: 0 when empty, 1-15 by fill ratio. */
    public static <T extends Resource> int getRedstoneSignalFromResourceHandler(ResourceHandler<T> handler) {
        if (handler.size() == 0) {
            return 0;
        }
        double ratio = 0;
        boolean any = false;
        for (int index = 0; index < handler.size(); index++) {
            T resource = handler.getResource(index);
            long amount = handler.getAmountAsLong(index);
            if (!resource.isEmpty() && amount > 0) {
                any = true;
                long capacity = handler.getCapacityAsLong(index, resource);
                ratio += capacity <= 0 ? 1 : Math.min(1, (double) amount / capacity);
            }
        }
        if (!any) {
            return 0;
        }
        return 1 + (int) (ratio / handler.size() * 14);
    }

    /** Inserts into indices already holding the resource first, then into the rest. */
    public static <T extends Resource> int insertStacking(ResourceHandler<T> handler, T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        int inserted = 0;
        for (int index = 0; index < handler.size() && inserted < amount; index++) {
            if (resource.equals(handler.getResource(index))) {
                inserted += handler.insert(index, resource, amount - inserted, transaction);
            }
        }
        for (int index = 0; index < handler.size() && inserted < amount; index++) {
            if (handler.getResource(index).isEmpty()) {
                inserted += handler.insert(index, resource, amount - inserted, transaction);
            }
        }
        return inserted;
    }

    public static <T extends Resource> ResourceStack<T> extractFirst(ResourceHandler<T> handler, Predicate<T> filter, int amount, TransactionContext transaction) {
        for (int index = 0; index < handler.size(); index++) {
            T resource = handler.getResource(index);
            if (!resource.isEmpty() && filter.test(resource)) {
                int extracted = handler.extract(index, resource, amount, transaction);
                if (extracted > 0) {
                    return new ResourceStack<>(resource, extracted);
                }
            }
        }
        return null;
    }

    public static <T extends Resource> int move(ResourceHandler<T> from, ResourceHandler<T> to, Predicate<T> filter, int amount, TransactionContext transaction) {
        return move(from, to, filter, amount, transaction, false);
    }

    public static <T extends Resource> int moveStacking(ResourceHandler<T> from, ResourceHandler<T> to, Predicate<T> filter, int amount,
          TransactionContext transaction) {
        return move(from, to, filter, amount, transaction, true);
    }

    private static <T extends Resource> int move(ResourceHandler<T> from, ResourceHandler<T> to, Predicate<T> filter, int amount, TransactionContext transaction,
          boolean stacking) {
        int moved = 0;
        for (int index = 0; index < from.size() && moved < amount; index++) {
            T resource = from.getResource(index);
            if (resource.isEmpty() || !filter.test(resource)) {
                continue;
            }
            try (Transaction nested = Transaction.open(transaction)) {
                int extracted = from.extract(index, resource, amount - moved, nested);
                if (extracted <= 0) {
                    continue;
                }
                int inserted = stacking ? insertStacking(to, resource, extracted, nested) : to.insert(resource, extracted, nested);
                if (inserted == extracted) {
                    nested.commit();
                    moved += inserted;
                } else if (inserted > 0) {
                    //Redo with the amount the target accepts
                    try (Transaction retry = Transaction.open(transaction)) {
                        int extractedAgain = from.extract(index, resource, inserted, retry);
                        int insertedAgain = stacking ? insertStacking(to, resource, extractedAgain, retry) : to.insert(resource, extractedAgain, retry);
                        if (extractedAgain == insertedAgain && insertedAgain > 0) {
                            retry.commit();
                            moved += insertedAgain;
                        }
                    }
                }
            }
        }
        return moved;
    }

    @Nullable
    public static <T extends Resource> ResourceStack<T> moveFirst(ResourceHandler<T> from, ResourceHandler<T> to, Predicate<T> filter, int amount,
          TransactionContext transaction) {
        return moveFirst(from, to, filter, amount, transaction, false);
    }

    @Nullable
    public static <T extends Resource> ResourceStack<T> moveFirstStacking(ResourceHandler<T> from, ResourceHandler<T> to, Predicate<T> filter, int amount,
          TransactionContext transaction) {
        return moveFirst(from, to, filter, amount, transaction, true);
    }

    @Nullable
    private static <T extends Resource> ResourceStack<T> moveFirst(ResourceHandler<T> from, ResourceHandler<T> to, Predicate<T> filter, int amount,
          TransactionContext transaction, boolean stacking) {
        for (int index = 0; index < from.size(); index++) {
            T resource = from.getResource(index);
            if (resource.isEmpty() || !filter.test(resource)) {
                continue;
            }
            int moved = move(from, to, filter.and(resource::equals), amount, transaction, stacking);
            if (moved > 0) {
                return new ResourceStack<>(resource, moved);
            }
        }
        return null;
    }

    public static <T extends Resource> boolean contains(ResourceHandler<T> handler, T resource) {
        return indexOf(handler, resource) != -1;
    }

    public static <T extends Resource> int indexOf(ResourceHandler<T> handler, T resource) {
        for (int index = 0; index < handler.size(); index++) {
            if (resource.equals(handler.getResource(index)) && handler.getAmountAsLong(index) > 0) {
                return index;
            }
        }
        return -1;
    }

    @Nullable
    public static <T extends Resource> T findExtractableResource(ResourceHandler<T> handler, Predicate<T> filter, TransactionContext transaction) {
        for (int index = 0; index < handler.size(); index++) {
            T resource = handler.getResource(index);
            if (!resource.isEmpty() && filter.test(resource)) {
                try (Transaction nested = Transaction.open(transaction)) {
                    if (handler.extract(index, resource, 1, nested) > 0) {
                        return resource;
                    }
                }
            }
        }
        return null;
    }
}
