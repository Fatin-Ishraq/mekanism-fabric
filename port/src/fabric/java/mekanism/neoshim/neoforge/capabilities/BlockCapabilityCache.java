package mekanism.neoshim.neoforge.capabilities;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

/**
 * Caches a block capability lookup and invalidates it when the capability may have changed (block entity added/removed, chunk
 * (un)loaded, or {@code Level#invalidateCapabilities} called). Semantics match NeoForge's BlockCapabilityCache.
 */
public final class BlockCapabilityCache<T, C> {

    public static <T, C> BlockCapabilityCache<T, C> create(BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context) {
        return create(capability, level, pos, context, () -> true, () -> {
        });
    }

    public static <T, C> BlockCapabilityCache<T, C> create(BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context,
          BooleanSupplier isValid, Runnable invalidationListener) {
        Objects.requireNonNull(capability);
        Objects.requireNonNull(isValid);
        Objects.requireNonNull(invalidationListener);
        pos = pos.immutable();
        BlockCapabilityCache<T, C> cache = new BlockCapabilityCache<>(capability, level, pos, context, isValid, invalidationListener);
        CapabilityInvalidation.registerListener(level, pos, cache.listener);
        return cache;
    }

    private final BlockCapability<T, C> capability;
    private final ServerLevel level;
    private final BlockPos pos;
    private final C context;
    private boolean cacheValid = false;
    @Nullable
    private T cachedCap = null;
    private final ICapabilityInvalidationListener listener;

    private BlockCapabilityCache(BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context, BooleanSupplier isValid, Runnable invalidationListener) {
        this.capability = capability;
        this.level = level;
        this.pos = pos;
        this.context = context;
        this.listener = () -> {
            if (!isValid.getAsBoolean()) {
                return false;
            }
            this.cacheValid = false;
            invalidationListener.run();
            return true;
        };
    }

    public ServerLevel level() {
        return level;
    }

    public BlockPos pos() {
        return pos;
    }

    public C context() {
        return context;
    }

    @Nullable
    public T getCapability() {
        if (!level.isLoaded(pos)) {
            //Don't cache when unloaded; NeoForge returns null here
            return null;
        }
        if (!cacheValid) {
            cachedCap = capability.getCapability(level, pos, null, null, context);
            cacheValid = true;
        }
        return cachedCap;
    }
}
