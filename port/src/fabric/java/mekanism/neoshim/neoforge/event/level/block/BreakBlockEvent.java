package mekanism.neoshim.neoforge.event.level.block;

import mekanism.neoshim.bus.api.ICancellableEvent;
import mekanism.neoshim.neoforge.event.level.BlockEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/// Fired before a block is broken; matches NeoForge 26.1, where {@code BreakBlockEvent} is its own class rather than nested in {@link BlockEvent}.
public class BreakBlockEvent extends BlockEvent implements ICancellableEvent {

    private final Player player;
    private boolean notifyClient = true;

    public BreakBlockEvent(Level level, BlockPos pos, BlockState state, Player player) {
        super(level, pos, state);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean shouldNotifyClient() {
        return notifyClient;
    }

    public void setNotifyClient(boolean notifyClient) {
        this.notifyClient = notifyClient;
    }
}
