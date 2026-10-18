package mekanism.neoshim.neoforge.common.world.chunk;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

/**
 * Forced-chunk controller. Tickets are added through a vanilla {@link TicketType} and persisted per level by {@link ForcedChunkStore}, which reinstates them on
 * server start and runs the controller's {@link LoadingValidationCallback} like NeoForge does.
 */
public record TicketController(ResourceLocation id, @Nullable LoadingValidationCallback callback) {

    static final Map<ResourceLocation, TicketController> CONTROLLERS = new ConcurrentHashMap<>();
    private static final TicketType<BlockPos> BLOCK_TICKET = TicketType.create("mekanism_fabric:forced_block", Comparator.comparingLong(BlockPos::asLong));

    public TicketController {
        CONTROLLERS.put(id, this);
    }

    public TicketController(ResourceLocation id) {
        this(id, null);
    }

    public boolean forceChunk(ServerLevel level, BlockPos owner, int chunkX, int chunkZ, boolean add, boolean ticking) {
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        //Level 2 = entity ticking for ticking tickets, 3 = block ticking (approximation of NeoForge's behavior)
        int distance = ticking ? 2 : 3;
        if (add) {
            level.getChunkSource().addRegionTicket(BLOCK_TICKET, chunkPos, distance, owner.immutable());
        } else {
            level.getChunkSource().removeRegionTicket(BLOCK_TICKET, chunkPos, distance, owner.immutable());
        }
        ForcedChunkStore.get(level).update(id, owner, chunkPos.toLong(), add, ticking);
        return true;
    }

    public boolean forceChunk(ServerLevel level, net.minecraft.world.entity.Entity owner, int chunkX, int chunkZ, boolean add, boolean ticking) {
        return forceChunk(level, owner.blockPosition(), chunkX, chunkZ, add, ticking);
    }
}
