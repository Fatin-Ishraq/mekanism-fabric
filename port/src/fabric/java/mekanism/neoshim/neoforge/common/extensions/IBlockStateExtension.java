package mekanism.neoshim.neoforge.common.extensions;

import mekanism.neoshim.neoforge.common.ItemAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link BlockBehaviour.BlockStateBase}; delegates to {@link IBlockExtension}. */
public interface IBlockStateExtension {

    private BlockState self() {
        return (BlockState) this;
    }

    default int getLightEmission(BlockGetter level, BlockPos pos) {
        return self().getBlock().getLightEmission(self(), level, pos);
    }

    default float getExplosionResistance(BlockGetter level, BlockPos pos, Explosion explosion) {
        return self().getBlock().getExplosionResistance(self(), level, pos, explosion);
    }

    default ItemStack getCloneItemStack(BlockPos pos, LevelReader level, boolean includeData, Player player) {
        return self().getBlock().getCloneItemStack(level, pos, self(), includeData, player);
    }

    default boolean onDestroyedByPlayer(Level level, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest, FluidState fluid) {
        return self().getBlock().onDestroyedByPlayer(self(), level, pos, player, toolStack, willHarvest, fluid);
    }

    default void onBlockExploded(ServerLevel level, BlockPos pos, Explosion explosion) {
        self().getBlock().onBlockExploded(self(), level, pos, explosion);
    }

    @Nullable
    default PathType getBlockPathType(BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        return self().getBlock().getBlockPathType(self(), level, pos, mob);
    }

    default boolean isFlammable(BlockGetter level, BlockPos pos, Direction face) {
        return self().getBlock().isFlammable(self(), level, pos, face);
    }

    default void onCaughtFire(Level level, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
        self().getBlock().onCaughtFire(self(), level, pos, face, igniter);
    }

    default void onNeighborChange(LevelReader level, BlockPos pos, BlockPos neighbor) {
        self().getBlock().onNeighborChange(self(), level, pos, neighbor);
    }

    default boolean canRedstoneConnectTo(BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return self().getBlock().canConnectRedstone(self(), level, pos, direction);
    }

    @Nullable
    default BlockState getToolModifiedState(UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        BlockState state = self().getBlock().getToolModifiedState(self(), context, itemAbility, simulate);
        return state;
    }

    default PushReaction getPistonPushReactionWithHook() {
        PushReaction reaction = self().getBlock().getPistonPushReaction(self());
        return reaction != null ? reaction : self().getPistonPushReaction();
    }

    default net.minecraft.world.level.block.SoundType getSoundType(LevelReader level, BlockPos pos, @Nullable net.minecraft.world.entity.Entity entity) {
        return self().getBlock().getSoundType(self(), level, pos, entity);
    }

    default float getFriction(LevelReader level, BlockPos pos, @Nullable net.minecraft.world.entity.Entity entity) {
        return self().getBlock().getFriction(self(), level, pos, entity);
    }

    default int getFlammability(BlockGetter level, BlockPos pos, Direction face) {
        return self().getBlock().getFlammability(self(), level, pos, face);
    }

    default boolean getWeakChanges(LevelReader level, BlockPos pos) {
        return self().getBlock().getWeakChanges(self(), level, pos);
    }

    default boolean shouldCheckWeakPower(net.minecraft.world.level.SignalGetter level, BlockPos pos, Direction side) {
        return self().getBlock().shouldCheckWeakPower(self(), level, pos, side);
    }

    default boolean isPortalFrame(BlockGetter level, BlockPos pos) {
        return self().getBlock().isPortalFrame(self(), level, pos);
    }
}
