package mekanism.neoshim.neoforge.event;

import mekanism.neoshim.neoforge.common.util.BlockSnapshot;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

/** NeoForge event hooks Mekanism calls directly. Fabric has no direct equivalents for these, so they report "not cancelled". */
public class EventHooks {

    /** Fabric has no neighbour-notify event; never cancelled. */
    public static mekanism.neoshim.neoforge.event.level.BlockEvent.NeighborNotifyEvent onNeighborNotify(Level level, net.minecraft.core.BlockPos pos,
          net.minecraft.world.level.block.state.BlockState state, java.util.EnumSet<Direction> notifiedSides, boolean forceRedstoneUpdate) {
        return new mekanism.neoshim.neoforge.event.level.BlockEvent.NeighborNotifyEvent(level, pos, state, notifiedSides, forceRedstoneUpdate);
    }

    public static boolean onBlockPlace(@Nullable Entity entity, BlockSnapshot blockSnapshot, Direction direction) {
        return false;
    }

    public static void firePlayerCraftingEvent(Player player, ItemStack crafted, Container craftMatrix) {
    }

    public static boolean onEntityStruckByLightning(Entity entity, LightningBolt bolt) {
        return false;
    }

    public static boolean onExplosionStart(Level level, Explosion explosion) {
        return false;
    }

    public static boolean onProjectileImpact(Projectile projectile, HitResult ray) {
        return false;
    }
}
