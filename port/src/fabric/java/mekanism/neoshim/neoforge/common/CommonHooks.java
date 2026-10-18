package mekanism.neoshim.neoforge.common;

import mekanism.neoshim.neoforge.event.level.BlockEvent;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CommonHooks {

    private static final ThreadLocal<Player> CRAFTING_PLAYER = new ThreadLocal<>();

    public static void setCraftingPlayer(@Nullable Player player) {
        CRAFTING_PLAYER.set(player);
    }

    @Nullable
    public static Player getCraftingPlayer() {
        return CRAFTING_PLAYER.get();
    }

    /** Fires Fabric's PlayerBlockBreakEvents.BEFORE (and the shim BreakEvent); the returned event is cancelled if any listener vetoed. */
    public static BlockEvent.BreakEvent fireBlockBreak(Level level, GameType gameType, ServerPlayer player, BlockPos pos, BlockState state) {
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, player);
        if (gameType.isBlockPlacingRestricted() && !player.mayBuild()) {
            event.setCanceled(true);
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!event.isCanceled() && !PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, pos, state, blockEntity)) {
            event.setCanceled(true);
        }
        if (!event.isCanceled()) {
            NeoForge.EVENT_BUS.post(event);
        }
        return event;
    }

    public static mekanism.neoshim.neoforge.event.entity.living.LivingShieldBlockEvent onDamageBlock(net.minecraft.world.entity.LivingEntity blocker,
          mekanism.neoshim.neoforge.common.damagesource.DamageContainer container, boolean originalBlocked) {
        return NeoForge.EVENT_BUS.post(new mekanism.neoshim.neoforge.event.entity.living.LivingShieldBlockEvent(blocker, container, originalBlocked));
    }

    public static boolean tryDispenseShearsHarvestBlock(net.minecraft.core.dispenser.BlockSource source, net.minecraft.world.item.ItemStack stack,
          net.minecraft.server.level.ServerLevel level, BlockPos pos) {
        return false;
    }
}
