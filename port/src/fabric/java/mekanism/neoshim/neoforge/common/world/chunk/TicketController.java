package mekanism.neoshim.neoforge.common.world.chunk;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

/**
 * Forced-chunk controller. Tickets are added through a vanilla {@link TicketType}; re-validation of persisted tickets on world load is driven
 * by Mekanism itself (its chunk loaders re-request their chunks when their block entities load). TODO(phase 5): persist tickets like NeoForge.
 */
public record TicketController(ResourceLocation id, @Nullable LoadingValidationCallback callback) {

    private static final TicketType<BlockPos> BLOCK_TICKET = TicketType.create("mekanism_fabric:forced_block", Comparator.comparingLong(BlockPos::asLong));

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
        return true;
    }

    public boolean forceChunk(ServerLevel level, net.minecraft.world.entity.Entity owner, int chunkX, int chunkZ, boolean add, boolean ticking) {
        return forceChunk(level, owner.blockPosition(), chunkX, chunkZ, add, ticking);
    }
}
