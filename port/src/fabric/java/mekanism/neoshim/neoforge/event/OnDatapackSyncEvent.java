package mekanism.neoshim.neoforge.event;

import java.util.List;
import java.util.stream.Stream;
import mekanism.neoshim.bus.api.Event;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;

public class OnDatapackSyncEvent extends Event {

    private final PlayerList playerList;
    @Nullable
    private final ServerPlayer player;

    public OnDatapackSyncEvent(PlayerList playerList, @Nullable ServerPlayer player) {
        this.playerList = playerList;
        this.player = player;
    }

    public PlayerList getPlayerList() {
        return playerList;
    }

    @Nullable
    public ServerPlayer getPlayer() {
        return player;
    }

    public Stream<ServerPlayer> getRelevantPlayers() {
        return player == null ? playerList.getPlayers().stream() : Stream.of(player);
    }

    public List<ServerPlayer> players() {
        return getRelevantPlayers().toList();
    }
}
