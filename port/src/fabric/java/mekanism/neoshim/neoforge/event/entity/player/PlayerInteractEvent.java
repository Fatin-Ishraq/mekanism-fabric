package mekanism.neoshim.neoforge.event.entity.player;

import mekanism.neoshim.bus.api.ICancellableEvent;
import mekanism.neoshim.neoforge.common.util.TriState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public abstract class PlayerInteractEvent extends PlayerEvent {

    private final InteractionHand hand;
    private final BlockPos pos;
    @Nullable
    private final Direction face;
    private InteractionResult cancellationResult = InteractionResult.PASS;

    protected PlayerInteractEvent(Player player, InteractionHand hand, BlockPos pos, @Nullable Direction face) {
        super(player);
        this.hand = hand;
        this.pos = pos;
        this.face = face;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public ItemStack getItemStack() {
        return getEntity().getItemInHand(hand);
    }

    public BlockPos getPos() {
        return pos;
    }

    @Nullable
    public Direction getFace() {
        return face;
    }

    public Level getLevel() {
        return getEntity().level();
    }

    public InteractionResult getCancellationResult() {
        return cancellationResult;
    }

    public void setCancellationResult(InteractionResult result) {
        this.cancellationResult = result;
    }

    public static class RightClickBlock extends PlayerInteractEvent implements ICancellableEvent {

        private TriState useBlock = TriState.DEFAULT;
        private TriState useItem = TriState.DEFAULT;
        private final BlockHitResult hitVec;

        public RightClickBlock(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hitVec) {
            super(player, hand, pos, hitVec.getDirection());
            this.hitVec = hitVec;
        }

        public TriState getUseBlock() {
            return useBlock;
        }

        public TriState getUseItem() {
            return useItem;
        }

        public BlockHitResult getHitVec() {
            return hitVec;
        }

        public void setUseBlock(TriState triggerBlock) {
            this.useBlock = triggerBlock;
        }

        public void setUseItem(TriState triggerItem) {
            this.useItem = triggerItem;
        }
    }

    public static class RightClickItem extends PlayerInteractEvent implements ICancellableEvent {

        public RightClickItem(Player player, InteractionHand hand) {
            super(player, hand, player.blockPosition(), null);
        }
    }

    public static class LeftClickBlock extends PlayerInteractEvent implements ICancellableEvent {

        public LeftClickBlock(Player player, BlockPos pos, Direction face) {
            super(player, InteractionHand.MAIN_HAND, pos, face);
        }
    }
}
