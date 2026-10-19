package mekanism.neoshim.neoforge.common.extensions;

import mekanism.neoshim.neoforge.capabilities.BlockCapability;
import mekanism.neoshim.neoforge.capabilities.CapabilityInvalidation;
import mekanism.neoshim.neoforge.capabilities.ICapabilityInvalidationListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link Level}. */
public interface ILevelExtension {

    private Level self() {
        return (Level) this;
    }

    @Nullable
    default <T, C> T getCapability(BlockCapability<T, C> cap, BlockPos pos, C context) {
        return cap.getCapability(self(), pos, null, null, context);
    }

    @Nullable
    default <T, C> T getCapability(BlockCapability<T, C> cap, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, C context) {
        return cap.getCapability(self(), pos, state, blockEntity, context);
    }

    @Nullable
    default <T> T getCapability(BlockCapability<T, Void> cap, BlockPos pos) {
        return cap.getCapability(self(), pos, null, null, null);
    }

    default void invalidateCapabilities(BlockPos pos) {
        if (self() instanceof ServerLevel serverLevel) {
            CapabilityInvalidation.invalidate(serverLevel, pos);
        }
    }

    default void invalidateCapabilities(ChunkPos pos) {
        if (self() instanceof ServerLevel serverLevel) {
            CapabilityInvalidation.invalidate(serverLevel, pos);
        }
    }

    default void registerCapabilityListener(BlockPos pos, ICapabilityInvalidationListener listener) {
        if (self() instanceof ServerLevel serverLevel) {
            CapabilityInvalidation.registerListener(serverLevel, pos.immutable(), listener);
        }
    }

    default Component getDescription() {
        return Component.literal(self().dimension().identifier().toString());
    }
}
