package mekanism.neoshim.neoforge.transfer.fluid;

import mekanism.neoshim.neoforge.capabilities.Capabilities;
import mekanism.neoshim.neoforge.fluids.FluidStack;
import mekanism.neoshim.neoforge.transfer.ResourceHandler;
import mekanism.neoshim.neoforge.transfer.ResourceHandlerUtil;
import mekanism.neoshim.neoforge.transfer.access.ItemAccess;
import mekanism.neoshim.neoforge.transfer.transaction.Transaction;
import mekanism.neoshim.neoforge.transfer.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** World/bucket helpers for fluid resource handlers. Placing and picking up use vanilla bucket behaviour (one bucket, 1000 mB, at a time). */
public final class FluidUtil {

    private static final int BUCKET = 1000;

    private FluidUtil() {
    }

    public static FluidStack getStack(ResourceHandler<FluidResource> handler, int index) {
        return handler.getResource(index).toStack(handler.getAmountAsInt(index));
    }

    public static FluidStack getFirstStackContained(ItemStack stack) {
        ResourceHandler<FluidResource> handler = ItemAccess.forStack(stack.copy()).getCapability(Capabilities.Fluid.ITEM);
        if (handler != null) {
            for (int index = 0; index < handler.size(); index++) {
                FluidResource resource = handler.getResource(index);
                if (!resource.isEmpty() && handler.getAmountAsLong(index) > 0) {
                    return resource.toStack(handler.getAmountAsInt(index));
                }
            }
        }
        return FluidStack.EMPTY;
    }

    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, Level level, BlockPos pos, @Nullable Direction side) {
        return interactWithFluidHandler(player, hand, level, pos, side, null);
    }

    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, Level level, BlockPos pos, @Nullable Direction side,
          @Nullable TransactionContext transaction) {
        ResourceHandler<FluidResource> handler = Capabilities.Fluid.BLOCK.getCapability(level, pos, null, null, side);
        return handler != null && interactWithFluidHandler(player, hand, pos, handler, transaction);
    }

    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, BlockPos pos, ResourceHandler<FluidResource> handler) {
        return interactWithFluidHandler(player, hand, pos, handler, null);
    }

    /** Empties the held container into the handler, or failing that fills it from the handler. */
    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, BlockPos pos, ResourceHandler<FluidResource> handler,
          @Nullable TransactionContext transaction) {
        ItemAccess access = ItemAccess.forPlayerInteraction(player, hand);
        ResourceHandler<FluidResource> item = access.getCapability(Capabilities.Fluid.ITEM);
        if (item == null) {
            return false;
        }
        Level level = player.level();
        Vec3 soundPos = Vec3.atCenterOf(pos);
        try (Transaction nested = Transaction.open(transaction)) {
            FluidResource moved = moveAny(item, handler, nested);
            if (moved != null) {
                nested.commit();
                triggerSoundAndGameEvent(moved, level, soundPos, player, false);
                return true;
            }
        }
        try (Transaction nested = Transaction.open(transaction)) {
            FluidResource moved = moveAny(handler, item, nested);
            if (moved != null) {
                nested.commit();
                triggerSoundAndGameEvent(moved, level, soundPos, player, true);
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static FluidResource moveAny(ResourceHandler<FluidResource> from, ResourceHandler<FluidResource> to, TransactionContext transaction) {
        for (int index = 0; index < from.size(); index++) {
            FluidResource resource = from.getResource(index);
            if (!resource.isEmpty() && ResourceHandlerUtil.move(from, to, resource::equals, Integer.MAX_VALUE, transaction) > 0) {
                return resource;
            }
        }
        return null;
    }

    public static void triggerSoundAndGameEvent(FluidResource resource, Level level, Vec3 pos, @Nullable Player player, boolean pickup) {
        boolean lava = resource.getFluid().is(FluidTags.LAVA);
        SoundEvent sound = pickup ? lava ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL : lava ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        level.playSound(null, pos.x, pos.y, pos.z, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, pickup ? GameEvent.FLUID_PICKUP : GameEvent.FLUID_PLACE, pos);
    }

    public static FluidStack tryPickupFluid(ResourceHandler<FluidResource> handler, @Nullable Player player, Level level, BlockPos pos, @Nullable Direction side) {
        return tryPickupFluid(handler, player, level, pos, side, null);
    }

    public static FluidStack tryPickupFluid(ResourceHandler<FluidResource> handler, @Nullable Player player, Level level, BlockPos pos,
          @Nullable TransactionContext transaction) {
        return tryPickupFluid(handler, player, level, pos, null, transaction);
    }

    /** Picks up a source block into the handler if the handler accepts the whole bucket. */
    public static FluidStack tryPickupFluid(ResourceHandler<FluidResource> handler, @Nullable Player player, Level level, BlockPos pos,
          @Nullable Direction side, @Nullable TransactionContext transaction) {
        BlockState state = level.getBlockState(pos);
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isSource() || !(state.getBlock() instanceof BucketPickup pickup)) {
            return FluidStack.EMPTY;
        }
        FluidResource resource = FluidResource.of(fluidState.getType());
        try (Transaction simulation = Transaction.open(transaction)) {
            if (handler.insert(resource, BUCKET, simulation) != BUCKET) {
                return FluidStack.EMPTY;
            }
        }
        ItemStack bucket = pickup.pickupBlock(player, level, pos, state);
        if (bucket.isEmpty()) {
            return FluidStack.EMPTY;
        }
        try (Transaction nested = Transaction.open(transaction)) {
            int inserted = handler.insert(resource, BUCKET, nested);
            nested.commit();
            triggerSoundAndGameEvent(resource, level, Vec3.atCenterOf(pos), player, true);
            return resource.toStack(inserted);
        }
    }

    public static FluidStack tryPlaceFluid(ResourceHandler<FluidResource> handler, @Nullable Player player, Level level, InteractionHand hand, BlockPos pos) {
        return tryPlaceFluid(handler, player, level, pos, true, null);
    }

    /** Places one bucket of the first placeable fluid in the handler. */
    public static FluidStack tryPlaceFluid(ResourceHandler<FluidResource> handler, @Nullable Player player, Level level, BlockPos pos, boolean playSound,
          @Nullable TransactionContext transaction) {
        for (int index = 0; index < handler.size(); index++) {
            FluidResource resource = handler.getResource(index);
            if (resource.isEmpty() || !(resource.getFluid().getBucket() instanceof BucketItem)) {
                continue;
            }
            try (Transaction nested = Transaction.open(transaction)) {
                if (handler.extract(index, resource, BUCKET, nested) != BUCKET) {
                    continue;
                }
                if (place(resource.getFluid(), player, level, pos)) {
                    nested.commit();
                    if (playSound) {
                        triggerSoundAndGameEvent(resource, level, Vec3.atCenterOf(pos), player, false);
                    }
                    return resource.toStack(BUCKET);
                }
            }
        }
        return FluidStack.EMPTY;
    }

    public static boolean tryPlaceFluid(FluidResource resource, @Nullable Player player, Level level, InteractionHand hand, BlockPos pos) {
        return tryPlaceFluid(resource, player, level, pos, true);
    }

    public static boolean tryPlaceFluid(FluidResource resource, @Nullable Player player, Level level, BlockPos pos, boolean playSound) {
        if (resource.isEmpty() || !place(resource.getFluid(), player, level, pos)) {
            return false;
        }
        if (playSound) {
            triggerSoundAndGameEvent(resource, level, Vec3.atCenterOf(pos), player, false);
        }
        return true;
    }

    private static boolean place(Fluid fluid, @Nullable Player player, Level level, BlockPos pos) {
        if (!(fluid.getBucket() instanceof BucketItem bucket)) {
            return false;
        }
        return bucket.emptyContents(player, level, pos, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }
}
