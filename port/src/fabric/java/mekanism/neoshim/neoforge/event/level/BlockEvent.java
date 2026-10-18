package mekanism.neoshim.neoforge.event.level;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.bus.api.ICancellableEvent;
import mekanism.neoshim.neoforge.common.util.BlockSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class BlockEvent extends Event {

    private final LevelAccessor level;
    private final BlockPos pos;
    private final BlockState state;

    protected BlockEvent(LevelAccessor level, BlockPos pos, BlockState state) {
        this.level = level;
        this.pos = pos;
        this.state = state;
    }

    public LevelAccessor getLevel() {
        return level;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public static class BreakEvent extends BlockEvent implements ICancellableEvent {

        private final Player player;

        public BreakEvent(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, Player player) {
            super(level, pos, state);
            this.player = player;
        }

        public Player getPlayer() {
            return player;
        }
    }

    public static class NeighborNotifyEvent extends BlockEvent implements ICancellableEvent {

        private final java.util.EnumSet<net.minecraft.core.Direction> notifiedSides;
        private final boolean forceRedstoneUpdate;

        public NeighborNotifyEvent(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, java.util.EnumSet<net.minecraft.core.Direction> notifiedSides,
              boolean forceRedstoneUpdate) {
            super(level, pos, state);
            this.notifiedSides = notifiedSides;
            this.forceRedstoneUpdate = forceRedstoneUpdate;
        }

        public java.util.EnumSet<net.minecraft.core.Direction> getNotifiedSides() {
            return notifiedSides;
        }

        public boolean getForceRedstoneUpdate() {
            return forceRedstoneUpdate;
        }
    }

    public static class EntityPlaceEvent extends BlockEvent implements ICancellableEvent {

        private final Entity entity;
        private final BlockSnapshot blockSnapshot;
        private final BlockState placedBlock;
        private final BlockState placedAgainst;

        public EntityPlaceEvent(BlockSnapshot blockSnapshot, BlockState placedAgainst, @Nullable Entity entity) {
            super(blockSnapshot.getLevel(), blockSnapshot.getPos(), blockSnapshot.getLevel().getBlockState(blockSnapshot.getPos()));
            this.entity = entity;
            this.blockSnapshot = blockSnapshot;
            this.placedBlock = getState();
            this.placedAgainst = placedAgainst;
        }

        @Nullable
        public Entity getEntity() {
            return entity;
        }

        public BlockSnapshot getBlockSnapshot() {
            return blockSnapshot;
        }

        public BlockState getPlacedBlock() {
            return placedBlock;
        }

        public BlockState getPlacedAgainst() {
            return placedAgainst;
        }
    }
}
