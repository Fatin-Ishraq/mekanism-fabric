package mekanism.neoshim.neoforge.common;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Injected into vanilla's {@link net.minecraft.world.entity.Shearable}; vanilla shearing spawns its drops itself, so none are returned. */
public interface IShearable {

    default boolean isShearable(@Nullable Player player, ItemStack item, Level level, BlockPos pos) {
        return !(this instanceof net.minecraft.world.entity.Shearable shearable) || shearable.readyForShearing();
    }

    default List<ItemStack> onSheared(@Nullable Player player, ItemStack item, Level level, BlockPos pos) {
        if (this instanceof net.minecraft.world.entity.Shearable shearable && level instanceof ServerLevel serverLevel) {
            shearable.shear(serverLevel, player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS, item);
        }
        return List.of();
    }

    default void spawnShearedDrop(ServerLevel level, BlockPos pos, ItemStack drop) {
        net.minecraft.world.level.block.Block.popResource(level, pos, drop);
    }
}
