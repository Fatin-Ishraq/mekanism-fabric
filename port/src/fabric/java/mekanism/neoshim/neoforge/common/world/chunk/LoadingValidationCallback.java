package mekanism.neoshim.neoforge.common.world.chunk;

import net.minecraft.server.level.ServerLevel;

@FunctionalInterface
public interface LoadingValidationCallback {

    void validateTickets(ServerLevel level, TicketHelper ticketHelper);
}
