package mekanism.neoshim.neoforge.common.world.chunk;

import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;

public interface TicketHelper {

    Map<BlockPos, TicketSet> getBlockTickets();

    Map<UUID, TicketSet> getEntityTickets();

    void removeAllTickets(BlockPos owner);

    void removeAllTickets(UUID owner);

    void removeTicket(BlockPos owner, long chunk, boolean ticking);
}
