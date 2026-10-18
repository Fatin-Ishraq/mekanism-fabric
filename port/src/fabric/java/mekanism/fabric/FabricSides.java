package mekanism.fabric;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/** Tracks which threads belong to a (dedicated or integrated) server, for {@code EffectiveSide}. */
public class FabricSides {

    private static final Set<Thread> SERVER_THREADS = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public static void markServerThread(Thread thread) {
        SERVER_THREADS.add(thread);
    }

    public static boolean isServerThread(Thread thread) {
        if (SERVER_THREADS.contains(thread)) {
            return true;
        }
        String name = thread.getName();
        return name.equals("Server thread");
    }
}
