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
    /** Loads and simulates; not persisted by vanilla because {@link ForcedChunkStore} persists and reinstates the tickets itself. */
    private static final TicketType TICKET = net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.TICKET_TYPE,
          Identifier.fromNamespaceAndPath("mekanism_fabric", "forced"), new TicketType(TicketType.NO_TIMEOUT, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION));
    //Ticket levels: 31 - radius. Radius 2 keeps the chunk entity ticking, radius 1 block ticking (approximation of NeoForge's behavior)
    private static final int TICKING_RADIUS = 2;
    private static final int NON_TICKING_RADIUS = 1;

    /** Forces the ticket type's registration during mod init (built-in registries freeze afterwards). */
    public static void init() {
    }

    static void applyTicket(ServerLevel level, long chunk, boolean ticking, boolean add) {
        ChunkPos pos = ChunkPos.unpack(chunk);
        int radius = ticking ? TICKING_RADIUS : NON_TICKING_RADIUS;
        if (add) {
            level.getChunkSource().addTicketWithRadius(TICKET, pos, radius);
        } else {
            level.getChunkSource().removeTicketWithRadius(TICKET, pos, radius);
        }
    }

    public TicketController {
        CONTROLLERS.put(id, this);
    }

    public TicketController(Identifier id) {
        this(id, null);
    }

    public boolean forceChunk(ServerLevel level, BlockPos owner, int chunkX, int chunkZ, boolean add, boolean ticking) {
        long chunk = ChunkPos.pack(chunkX, chunkZ);
        //One ticket per owner and chunk: only touch the ticket when the stored state changes
        if (ForcedChunkStore.get(level).update(id, owner, chunk, add, ticking)) {
            applyTicket(level, chunk, ticking, add);
        }
        return true;
    }

    public boolean forceChunk(ServerLevel level, net.minecraft.world.entity.Entity owner, int chunkX, int chunkZ, boolean add, boolean ticking) {
        return forceChunk(level, owner.blockPosition(), chunkX, chunkZ, add, ticking);
    }
}
