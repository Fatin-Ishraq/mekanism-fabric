package mekanism.neoshim.neoforge.capabilities;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Block capability backed by a Fabric {@link BlockApiLookup} with the same id. */
public final class BlockCapability<T, C> extends BaseCapability<T, C> {

    private static final Map<Identifier, BlockCapability<?, ?>> REGISTRY = new ConcurrentHashMap<>();

    private final BlockApiLookup<T, C> lookup;
    private boolean proxyable;

    @SuppressWarnings("unchecked")
    public static <T, C> BlockCapability<T, C> create(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        return (BlockCapability<T, C>) REGISTRY.computeIfAbsent(name, n -> new BlockCapability<>(n, typeClass, contextClass));
    }

    public static <T> BlockCapability<T, @Nullable Direction> createSided(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, Direction.class);
    }

    public static <T> BlockCapability<T, Void> createVoid(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, Void.class);
    }

    public static synchronized List<BlockCapability<?, ?>> getAll() {
        return List.copyOf(REGISTRY.values());
    }

    private BlockCapability(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        super(name, typeClass, contextClass);
        this.lookup = BlockApiLookup.get(name, typeClass, contextClass);
    }

    public BlockApiLookup<T, C> lookup() {
        return lookup;
    }

    @Nullable
    public T getCapability(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, C context) {
        return lookup.find(level, pos, state, blockEntity, context);
    }

    public boolean isProxyable() {
        return proxyable;
    }

    void setProxyable() {
        this.proxyable = true;
    }
}
