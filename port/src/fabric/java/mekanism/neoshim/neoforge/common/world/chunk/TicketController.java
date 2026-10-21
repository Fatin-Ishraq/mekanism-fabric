package mekanism.neoshim.neoforge.common.world.chunk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

/**
 * Forced-chunk controller. Tickets are added through a vanilla {@link TicketType} and persisted per level by {@link ForcedChunkStore}, which reinstates them on
 * server start and runs the controller's {@link LoadingValidationCallback} like NeoForge does.
 */
public record TicketController(Identifier id, @Nullable LoadingValidationCallback callback) {

    static final Map<Identifier, TicketController> CONTROLLERS = new ConcurrentHashMap<>();
    //Custom ticket types are not persisted by vanilla (no FLAG_PERSIST): ForcedChunkStore owns persistence and reinstates on server start
    private static final TicketType LOADING_TICKET = new TicketType(TicketType.NO_TIMEOUT, TicketType.FLAG_LOADING);
    private static final TicketType TICKING_TICKET = new TicketType(TicketType.NO_TIMEOUT, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION);

    public TicketController {
        CONTROLLERS.put(id, this);
    }

    public TicketController(Identifier id) {
        this(id, null);
    }

    public boolean forceChunk(ServerLevel level, BlockPos owner, int chunkX, int chunkZ, boolean add, boolean ticking) {
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        //Level 2 = entity ticking for ticking tickets, 3 = block ticking (approximation of NeoForge's behavior)
        int distance = ticking ? 2 : 3;
        TicketType type = ticking ? TICKING_TICKET : LOADING_TICKET;
        if (add) {
            level.getChunkSource().addTicketWithRadius(type, chunkPos, distance);
        } else {
            level.getChunkSource().removeTicketWithRadius(type, chunkPos, distance);
        }
        ForcedChunkStore.get(level).update(id, owner, chunkPos.pack(), add, ticking);
        return true;
    }

    public boolean forceChunk(ServerLevel level, net.minecraft.world.entity.Entity owner, int chunkX, int chunkZ, boolean add, boolean ticking) {
        return forceChunk(level, owner.blockPosition(), chunkX, chunkZ, add, ticking);
    }
}
