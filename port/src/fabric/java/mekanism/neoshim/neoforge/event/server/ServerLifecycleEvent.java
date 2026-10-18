package mekanism.neoshim.neoforge.event.server;

import mekanism.neoshim.bus.api.Event;
import net.minecraft.server.MinecraftServer;

public abstract class ServerLifecycleEvent extends Event {

    protected final MinecraftServer server;

    protected ServerLifecycleEvent(MinecraftServer server) {
        this.server = server;
    }

    public MinecraftServer getServer() {
        return server;
    }
}
