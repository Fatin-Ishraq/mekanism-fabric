package mekanism.neoshim.neoforge.client.event;

import mekanism.neoshim.bus.api.Event;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.Nullable;

public abstract class ClientPlayerNetworkEvent extends Event {

    @Nullable
    private final MultiPlayerGameMode multiPlayerGameMode;
    @Nullable
    private final LocalPlayer player;
    @Nullable
    private final Connection connection;

    protected ClientPlayerNetworkEvent(@Nullable MultiPlayerGameMode multiPlayerGameMode, @Nullable LocalPlayer player, @Nullable Connection connection) {
        this.multiPlayerGameMode = multiPlayerGameMode;
        this.player = player;
        this.connection = connection;
    }

    @Nullable
    public MultiPlayerGameMode getMultiPlayerGameMode() {
        return multiPlayerGameMode;
    }

    @Nullable
    public LocalPlayer getPlayer() {
        return player;
    }

    @Nullable
    public Connection getConnection() {
        return connection;
    }

    public static class LoggingIn extends ClientPlayerNetworkEvent {

        public LoggingIn(MultiPlayerGameMode controller, LocalPlayer player, Connection networkManager) {
            super(controller, player, networkManager);
        }
    }

    public static class LoggingOut extends ClientPlayerNetworkEvent {

        public LoggingOut(@Nullable MultiPlayerGameMode controller, @Nullable LocalPlayer player, @Nullable Connection networkManager) {
            super(controller, player, networkManager);
        }
    }

    public static class Clone extends ClientPlayerNetworkEvent {

        private final LocalPlayer oldPlayer;

        public Clone(MultiPlayerGameMode pc, LocalPlayer oldPlayer, LocalPlayer newPlayer, Connection networkManager) {
            super(pc, newPlayer, networkManager);
            this.oldPlayer = oldPlayer;
        }

        public LocalPlayer getOldPlayer() {
            return oldPlayer;
        }

        public LocalPlayer getNewPlayer() {
            return super.getPlayer();
        }
    }
}
