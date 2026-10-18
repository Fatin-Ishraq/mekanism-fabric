package mekanism.neoshim.neoforge.server;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public class ServerLifecycleHooks {

    @Nullable
    private static volatile MinecraftServer currentServer;

    /** Maintained by the Fabric bridge from ServerLifecycleEvents. */
    public static void setCurrentServer(@Nullable MinecraftServer server) {
        currentServer = server;
    }

    @Nullable
    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }
}
