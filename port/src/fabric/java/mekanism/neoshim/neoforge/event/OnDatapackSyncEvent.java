package mekanism.neoshim.neoforge.event;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import mekanism.neoshim.bus.api.Event;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public class OnDatapackSyncEvent extends Event {

    private final PlayerList playerList;
    @Nullable
    private final ServerPlayer player;
    private final Set<RecipeType<?>> recipeTypesToSend = new LinkedHashSet<>();

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

    //Vanilla syncs every recipe type to joining clients, so collecting the list is all that is needed here
    public void sendRecipes(RecipeType<?>... types) {
        for (RecipeType<?> type : types) {
            recipeTypesToSend.add(type);
        }
    }

    public void sendRecipes(Iterable<RecipeType<?>> types) {
        for (RecipeType<?> type : types) {
            recipeTypesToSend.add(type);
        }
    }

    public Set<RecipeType<?>> getRecipeTypesToSend() {
        return recipeTypesToSend;
    }
}
