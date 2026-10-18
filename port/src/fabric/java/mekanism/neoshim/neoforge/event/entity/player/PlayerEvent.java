package mekanism.neoshim.neoforge.event.entity.player;

import java.util.Optional;
import mekanism.neoshim.bus.api.ICancellableEvent;
import mekanism.neoshim.neoforge.event.entity.living.LivingEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class PlayerEvent extends LivingEvent {

    private final Player player;

    protected PlayerEvent(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public Player getEntity() {
        return player;
    }

    public static class PlayerLoggedInEvent extends PlayerEvent {

        public PlayerLoggedInEvent(Player player) {
            super(player);
        }
    }

    public static class PlayerLoggedOutEvent extends PlayerEvent {

        public PlayerLoggedOutEvent(Player player) {
            super(player);
        }
    }

    public static class PlayerRespawnEvent extends PlayerEvent {

        private final boolean endConquered;

        public PlayerRespawnEvent(Player player, boolean endConquered) {
            super(player);
            this.endConquered = endConquered;
        }

        public boolean isEndConquered() {
            return endConquered;
        }
    }

    public static class PlayerChangedDimensionEvent extends PlayerEvent {

        private final ResourceKey<Level> fromDim;
        private final ResourceKey<Level> toDim;

        public PlayerChangedDimensionEvent(Player player, ResourceKey<Level> fromDim, ResourceKey<Level> toDim) {
            super(player);
            this.fromDim = fromDim;
            this.toDim = toDim;
        }

        public ResourceKey<Level> getFrom() {
            return fromDim;
        }

        public ResourceKey<Level> getTo() {
            return toDim;
        }
    }

    public static class Clone extends PlayerEvent {

        private final Player original;
        private final boolean wasDeath;

        public Clone(Player newPlayer, Player oldPlayer, boolean wasDeath) {
            super(newPlayer);
            this.original = oldPlayer;
            this.wasDeath = wasDeath;
        }

        public Player getOriginal() {
            return original;
        }

        public boolean isWasDeath() {
            return wasDeath;
        }
    }

    public static class StartTracking extends PlayerEvent {

        private final net.minecraft.world.entity.Entity target;

        public StartTracking(Player player, net.minecraft.world.entity.Entity target) {
            super(player);
            this.target = target;
        }

        public net.minecraft.world.entity.Entity getTarget() {
            return target;
        }
    }

    public static class StopTracking extends PlayerEvent {

        private final net.minecraft.world.entity.Entity target;

        public StopTracking(Player player, net.minecraft.world.entity.Entity target) {
            super(player);
            this.target = target;
        }

        public net.minecraft.world.entity.Entity getTarget() {
            return target;
        }
    }

    public static class ItemCraftedEvent extends PlayerEvent {

        private final ItemStack crafting;
        private final Container craftMatrix;

        public ItemCraftedEvent(Player player, ItemStack crafting, Container craftMatrix) {
            super(player);
            this.crafting = crafting;
            this.craftMatrix = craftMatrix;
        }

        public ItemStack getCrafting() {
            return this.crafting;
        }

        public Container getInventory() {
            return this.craftMatrix;
        }
    }

    public static class BreakSpeed extends PlayerEvent implements ICancellableEvent {

        private final BlockState state;
        private final float originalSpeed;
        private float newSpeed;
        @Nullable
        private final BlockPos pos;

        public BreakSpeed(Player player, BlockState state, float original, @Nullable BlockPos pos) {
            super(player);
            this.state = state;
            this.originalSpeed = original;
            this.newSpeed = original;
            this.pos = pos != null ? pos.immutable() : null;
        }

        public BlockState getState() {
            return state;
        }

        public float getOriginalSpeed() {
            return originalSpeed;
        }

        public float getNewSpeed() {
            return newSpeed;
        }

        public void setNewSpeed(float newSpeed) {
            this.newSpeed = newSpeed;
        }

        public Optional<BlockPos> getPosition() {
            return Optional.ofNullable(pos);
        }
    }
}
