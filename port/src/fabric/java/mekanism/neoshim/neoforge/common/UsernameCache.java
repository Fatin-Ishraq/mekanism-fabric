package mekanism.neoshim.neoforge.common;

import java.util.Optional;
import java.util.UUID;
import mekanism.neoshim.neoforge.server.ServerLifecycleHooks;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/** Backed by the server's profile cache (NeoForge keeps its own usernamecache.json). */
public class UsernameCache {

    @Nullable
    public static String getLastKnownUsername(UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.services().nameToIdCache().get(uuid).map(net.minecraft.server.players.NameAndId::name).orElse(null);
        }
        return null;
    }

    public static Optional<String> lookup(UUID uuid) {
        return Optional.ofNullable(getLastKnownUsername(uuid));
    }
}
