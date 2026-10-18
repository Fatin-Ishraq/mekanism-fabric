package mekanism.neoshim.neoforge.capabilities;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * Per-level registry of {@link ICapabilityInvalidationListener}s, keyed by block position. Invalidation is triggered from the Fabric bridge
 * (block entity load/unload, chunk load/unload) and from {@code Level#invalidateCapabilities}.
 */
public final class CapabilityInvalidation {

    private static final Map<ServerLevel, Long2ObjectOpenHashMap<List<ICapabilityInvalidationListener>>> LISTENERS = new WeakHashMap<>();

    private CapabilityInvalidation() {
    }

    public static synchronized void registerListener(ServerLevel level, BlockPos pos, ICapabilityInvalidationListener listener) {
        LISTENERS.computeIfAbsent(level, l -> new Long2ObjectOpenHashMap<>()).computeIfAbsent(pos.asLong(), p -> new ArrayList<>()).add(listener);
    }

    public static void invalidate(ServerLevel level, BlockPos pos) {
        List<ICapabilityInvalidationListener> listeners;
        synchronized (CapabilityInvalidation.class) {
            Long2ObjectOpenHashMap<List<ICapabilityInvalidationListener>> map = LISTENERS.get(level);
            if (map == null) {
                return;
            }
            listeners = map.remove(pos.asLong());
        }
        if (listeners == null) {
            return;
        }
        List<ICapabilityInvalidationListener> kept = new ArrayList<>(listeners.size());
        for (ICapabilityInvalidationListener listener : listeners) {
            if (listener.onInvalidate()) {
                kept.add(listener);
            }
        }
        if (!kept.isEmpty()) {
            synchronized (CapabilityInvalidation.class) {
                LISTENERS.computeIfAbsent(level, l -> new Long2ObjectOpenHashMap<>()).computeIfAbsent(pos.asLong(), p -> new ArrayList<>()).addAll(kept);
            }
        }
    }

    public static void invalidate(ServerLevel level, ChunkPos chunkPos) {
        List<Long> positions = new ArrayList<>();
        synchronized (CapabilityInvalidation.class) {
            Long2ObjectOpenHashMap<List<ICapabilityInvalidationListener>> map = LISTENERS.get(level);
            if (map == null) {
                return;
            }
            for (long key : map.keySet()) {
                if (BlockPos.getX(key) >> 4 == chunkPos.x && BlockPos.getZ(key) >> 4 == chunkPos.z) {
                    positions.add(key);
                }
            }
        }
        for (long key : positions) {
            invalidate(level, BlockPos.of(key));
        }
    }

    public static synchronized void clear(ServerLevel level) {
        LISTENERS.remove(level);
    }
}
