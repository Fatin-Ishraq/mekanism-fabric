package mekanism.neoshim.neoforge.server.permission;

import java.util.UUID;
import mekanism.neoshim.neoforge.server.permission.nodes.PermissionDynamicContext;
import mekanism.neoshim.neoforge.server.permission.nodes.PermissionNode;
import net.minecraft.server.level.ServerPlayer;

/** Evaluates permission nodes with their default resolvers (no external permission backend yet). */
public final class PermissionAPI {

    private PermissionAPI() {
    }

    public static <T> T getPermission(ServerPlayer player, PermissionNode<T> node, PermissionDynamicContext<?>... context) {
        return node.getDefaultResolver().resolve(player, player.getUUID(), context);
    }

    public static <T> T getOfflinePermission(UUID player, PermissionNode<T> node, PermissionDynamicContext<?>... context) {
        return node.getDefaultResolver().resolve(null, player, context);
    }
}
