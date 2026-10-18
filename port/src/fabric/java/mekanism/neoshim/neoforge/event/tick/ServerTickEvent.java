package mekanism.neoshim.neoforge.event.tick;

import java.util.function.BooleanSupplier;
import mekanism.neoshim.bus.api.Event;
import net.minecraft.server.MinecraftServer;

public abstract class ServerTickEvent extends Event {

    private final BooleanSupplier hasTime;
    private final MinecraftServer server;

    protected ServerTickEvent(BooleanSupplier hasTime, MinecraftServer server) {
        this.hasTime = hasTime;
        this.server = server;
    }

    public boolean hasTime() {
        return hasTime.getAsBoolean();
    }

    public MinecraftServer getServer() {
        return server;
    }

    public static class Pre extends ServerTickEvent {

        public Pre(BooleanSupplier hasTime, MinecraftServer server) {
            super(hasTime, server);
        }
    }

    public static class Post extends ServerTickEvent {

        public Post(BooleanSupplier hasTime, MinecraftServer server) {
            super(hasTime, server);
        }
    }
}
