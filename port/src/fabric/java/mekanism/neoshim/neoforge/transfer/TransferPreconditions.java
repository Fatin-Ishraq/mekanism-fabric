package mekanism.neoshim.neoforge.transfer;

import mekanism.neoshim.neoforge.transfer.resource.Resource;

public class TransferPreconditions {

    public static void checkNonEmpty(Resource resource) {
        if (resource.isEmpty()) {
            throw new IllegalArgumentException("Expected resource to be non-empty: " + resource);
        }
    }

    public static void checkNonNegative(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Expected amount to be non-negative: " + amount);
        }
    }

    public static void checkNonEmptyNonNegative(Resource resource, int amount) {
        checkNonEmpty(resource);
        checkNonNegative(amount);
    }
}
