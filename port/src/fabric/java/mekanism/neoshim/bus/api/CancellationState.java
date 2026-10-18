package mekanism.neoshim.bus.api;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Backing storage for {@link ICancellableEvent} (interfaces cannot hold state). */
final class CancellationState {

    private static final Map<Object, Boolean> CANCELED = Collections.synchronizedMap(new WeakHashMap<>());

    private CancellationState() {
    }

    static void set(Object event, boolean canceled) {
        if (canceled) {
            CANCELED.put(event, Boolean.TRUE);
        } else {
            CANCELED.remove(event);
        }
    }

    static boolean get(Object event) {
        return CANCELED.containsKey(event);
    }
}
