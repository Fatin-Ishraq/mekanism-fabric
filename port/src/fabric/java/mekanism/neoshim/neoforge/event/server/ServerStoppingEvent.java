package mekanism.neoshim.neoforge.event.server;

import net.minecraft.server.MinecraftServer;

public class ServerStoppingEvent extends ServerLifecycleEvent {

    public ServerStoppingEvent(MinecraftServer server) {
        super(server);
    }
}
