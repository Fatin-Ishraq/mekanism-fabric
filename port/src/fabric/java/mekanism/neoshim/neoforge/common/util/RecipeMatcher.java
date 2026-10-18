package mekanism.neoshim.neoforge.common.util;

import java.util.BitSet;
import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

/** Bipartite matching of inputs to tests (same contract as NeoForge's RecipeMatcher#findMatches). */
public class RecipeMatcher {

    @Nullable
    public static <T> int[] findMatches(List<T> inputs, List<? extends Predicate<T>> tests) {
        int elements = inputs.size();
        if (elements != tests.size()) {
            return null;
        }
        int[] ret = new int[elements];
        java.util.Arrays.fill(ret, -1);
        BitSet[] data = new BitSet[elements];
        for (int x = 0; x < elements; x++) {
            data[x] = new BitSet(elements);
            for (int y = 0; y < elements; y++) {
                if (tests.get(x).test(inputs.get(y))) {
                    data[x].set(y);
                }
            }
        }
        int[] inputToTest = new int[elements];
        java.util.Arrays.fill(inputToTest, -1);
        for (int test = 0; test < elements; test++) {
            if (!augment(test, data, inputToTest, new boolean[elements])) {
                return null;
            }
        }
        for (int input = 0; input < elements; input++) {
            ret[input] = inputToTest[input];
        }
        return ret;
    }

    private static boolean augment(int test, BitSet[] data, int[] inputToTest, boolean[] seen) {
        for (int input = data[test].nextSetBit(0); input >= 0; input = data[test].nextSetBit(input + 1)) {
            if (!seen[input]) {
                seen[input] = true;
                if (inputToTest[input] == -1 || augment(inputToTest[input], data, inputToTest, seen)) {
                    inputToTest[input] = test;
                    return true;
                }
            }
        }
        return false;
    }
}
