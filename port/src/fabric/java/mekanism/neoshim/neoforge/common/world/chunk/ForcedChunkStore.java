package mekanism.neoshim.neoforge.common.world.chunk;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Per-level persistence of forced chunks owned by {@link TicketController}s, mirroring NeoForge's ForcedChunkManager: tickets are saved with the level, reinstated
 * when the server starts, and then handed to the owning controller's {@link LoadingValidationCallback}. Tickets of controllers that no longer exist are dropped.
 */
public final class ForcedChunkStore extends SavedData {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String NAME = "mekanism_fabric_forced_chunks";
    private static final SavedData.Factory<ForcedChunkStore> FACTORY = new SavedData.Factory<>(ForcedChunkStore::new, ForcedChunkStore::load, null);

    /** controller id -> owner position -> chunks */
    private final Map<Identifier, Map<BlockPos, TicketSet>> blockTickets = new HashMap<>();

    public static ForcedChunkStore get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    void update(Identifier controller, BlockPos owner, long chunk, boolean add, boolean ticking) {
        if (add) {
            TicketSet set = blockTickets.computeIfAbsent(controller, c -> new HashMap<>()).computeIfAbsent(owner.immutable(),
                  o -> new TicketSet(new LongOpenHashSet(), new LongOpenHashSet()));
            if ((ticking ? set.ticking() : set.nonTicking()).add(chunk)) {
                setDirty();
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
                }
            }
        }
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
                    LOGGER.warn("Dropping forced chunks of unknown ticket controller {} in {}", entry.getKey(), level.dimension().location());
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
                    set.nonTicking().forEach(chunk -> controller.forceChunk(level, pos, ChunkPos.getX(chunk), ChunkPos.getZ(chunk), true, false));
                    set.ticking().forEach(chunk -> controller.forceChunk(level, pos, ChunkPos.getX(chunk), ChunkPos.getZ(chunk), true, true));
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

    private static ForcedChunkStore load(CompoundTag tag, HolderLookup.Provider provider) {
        ForcedChunkStore store = new ForcedChunkStore();
        for (String controllerId : tag.getAllKeys()) {
            Identifier controller = Identifier.tryParse(controllerId);
            if (controller == null) {
                continue;
            }
            Map<BlockPos, TicketSet> owners = new HashMap<>();
            for (Tag element : tag.getList(controllerId, Tag.TAG_COMPOUND)) {
                CompoundTag ownerTag = (CompoundTag) element;
                owners.put(BlockPos.of(ownerTag.getLong("pos")), new TicketSet(new LongOpenHashSet(ownerTag.getLongArray("chunks")),
                      new LongOpenHashSet(ownerTag.getLongArray("ticking"))));
            }
            if (!owners.isEmpty()) {
                store.blockTickets.put(controller, owners);
            }
        }
        return store;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        blockTickets.forEach((controller, owners) -> {
            ListTag list = new ListTag();
            owners.forEach((pos, set) -> {
                CompoundTag ownerTag = new CompoundTag();
                ownerTag.putLong("pos", pos.asLong());
                ownerTag.putLongArray("chunks", toArray(set.nonTicking()));
                ownerTag.putLongArray("ticking", toArray(set.ticking()));
                list.add(ownerTag);
            });
            tag.put(controller.toString(), list);
        });
        return tag;
    }

    private static long[] toArray(LongSet set) {
        return set.toLongArray();
    }
}
