package mekanism.neoshim.neoforge.common.world.chunk;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.LongStream;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Per-level persistence of forced chunks owned by {@link TicketController}s, mirroring NeoForge's ForcedChunkManager: tickets are saved with the level, reinstated
 * when the server starts, and then handed to the owning controller's {@link LoadingValidationCallback}. Tickets of controllers that no longer exist are dropped.
 */
public final class ForcedChunkStore extends SavedData {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final SavedDataType<ForcedChunkStore> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath("mekanism_fabric", "forced_chunks"),
          ForcedChunkStore::new, Codec.lazyInitialized(() -> ForcedChunkStore.CODEC), null);

    /** controller id -> owner position -> chunks */
    private final Map<Identifier, Map<BlockPos, TicketSet>> blockTickets = new HashMap<>();

    public static ForcedChunkStore get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    boolean update(Identifier controller, BlockPos owner, long chunk, boolean add, boolean ticking) {
        if (add) {
            TicketSet set = blockTickets.computeIfAbsent(controller, c -> new HashMap<>()).computeIfAbsent(owner.immutable(),
                  o -> new TicketSet(new LongOpenHashSet(), new LongOpenHashSet()));
            if ((ticking ? set.ticking() : set.nonTicking()).add(chunk)) {
                setDirty();
                return true;
            }
        } else {
            Map<BlockPos, TicketSet> owners = blockTickets.get(controller);
            if (owners != null) {
                TicketSet set = owners.get(owner);
                if (set != null && (ticking ? set.ticking() : set.nonTicking()).remove(chunk)) {
                    if (set.ticking().isEmpty() && set.nonTicking().isEmpty()) {
                        owners.remove(owner);
                        if (owners.isEmpty()) {
                            blockTickets.remove(controller);
                        }
                    }
                    setDirty();
                    return true;
                }
            }
        }
        return false;
    }

    /** Re-adds every saved ticket, then lets each controller validate its own. */
    public static void reinstate(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            ForcedChunkStore store = get(level);
            if (store.blockTickets.isEmpty()) {
                continue;
            }
            for (Iterator<Map.Entry<Identifier, Map<BlockPos, TicketSet>>> iterator = store.blockTickets.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<Identifier, Map<BlockPos, TicketSet>> entry = iterator.next();
                if (!TicketController.CONTROLLERS.containsKey(entry.getKey())) {
                    LOGGER.warn("Dropping forced chunks of unknown ticket controller {} in {}", entry.getKey(), level.dimension().identifier());
                    iterator.remove();
                    store.setDirty();
                }
            }
            //Copy, as reinstating goes through TicketController#forceChunk which mutates the store
            Map<Identifier, Map<BlockPos, TicketSet>> copy = new HashMap<>();
            store.blockTickets.forEach((controller, owners) -> {
                Map<BlockPos, TicketSet> ownersCopy = new HashMap<>();
                owners.forEach((pos, set) -> ownersCopy.put(pos, new TicketSet(new LongOpenHashSet(set.nonTicking()), new LongOpenHashSet(set.ticking()))));
                copy.put(controller, ownersCopy);
            });
            copy.forEach((controllerId, owners) -> {
                TicketController controller = TicketController.CONTROLLERS.get(controllerId);
                owners.forEach((pos, set) -> {
                    set.nonTicking().forEach(chunk -> TicketController.applyTicket(level, chunk, false, true));
                    set.ticking().forEach(chunk -> TicketController.applyTicket(level, chunk, true, true));
                });
                if (controller.callback() != null) {
                    controller.callback().validateTickets(level, new Helper(level, controller, owners));
                }
            });
        }
    }

    private record Helper(ServerLevel level, TicketController controller, Map<BlockPos, TicketSet> blockTickets) implements TicketHelper {

        @Override
        public Map<BlockPos, TicketSet> getBlockTickets() {
            return Collections.unmodifiableMap(blockTickets);
        }

        @Override
        public Map<UUID, TicketSet> getEntityTickets() {
            return Collections.emptyMap();
        }

        @Override
        public void removeAllTickets(BlockPos owner) {
            TicketSet set = blockTickets.get(owner);
            if (set != null) {
                set.nonTicking().forEach(chunk -> controller.forceChunk(level, owner, ChunkPos.getX(chunk), ChunkPos.getZ(chunk), false, false));
                set.ticking().forEach(chunk -> controller.forceChunk(level, owner, ChunkPos.getX(chunk), ChunkPos.getZ(chunk), false, true));
            }
        }

        @Override
        public void removeAllTickets(UUID owner) {
        }

        @Override
        public void removeTicket(BlockPos owner, long chunk, boolean ticking) {
            controller.forceChunk(level, owner, ChunkPos.getX(chunk), ChunkPos.getZ(chunk), false, ticking);
        }
    }

    private record OwnerEntry(BlockPos pos, long[] chunks, long[] ticking) {

        static final Codec<OwnerEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              BlockPos.CODEC.fieldOf("pos").forGetter(OwnerEntry::pos),
              Codec.LONG_STREAM.xmap(LongStream::toArray, LongStream::of).fieldOf("chunks").forGetter(OwnerEntry::chunks),
              Codec.LONG_STREAM.xmap(LongStream::toArray, LongStream::of).fieldOf("ticking").forGetter(OwnerEntry::ticking)
        ).apply(instance, OwnerEntry::new));
    }

    private static final Codec<ForcedChunkStore> CODEC = Codec.unboundedMap(Identifier.CODEC, OwnerEntry.CODEC.listOf()).xmap(map -> {
        ForcedChunkStore store = new ForcedChunkStore();
        map.forEach((controller, entries) -> {
            Map<BlockPos, TicketSet> owners = new HashMap<>();
            for (OwnerEntry entry : entries) {
                owners.put(entry.pos(), new TicketSet(new LongOpenHashSet(entry.chunks()), new LongOpenHashSet(entry.ticking())));
            }
            if (!owners.isEmpty()) {
                store.blockTickets.put(controller, owners);
            }
        });
        return store;
    }, store -> {
        Map<Identifier, List<OwnerEntry>> map = new HashMap<>();
        store.blockTickets.forEach((controller, owners) -> map.put(controller, owners.entrySet().stream()
              .map(e -> new OwnerEntry(e.getKey(), toArray(e.getValue().nonTicking()), toArray(e.getValue().ticking()))).toList()));
        return map;
    });

    private static long[] toArray(LongSet set) {
        return set.toLongArray();
    }
}
