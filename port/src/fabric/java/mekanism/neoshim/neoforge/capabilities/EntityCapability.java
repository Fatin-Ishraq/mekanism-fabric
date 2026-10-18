package mekanism.neoshim.neoforge.capabilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/** Entity capability backed by a Fabric {@link EntityApiLookup} with the same id. */
public final class EntityCapability<T, C> extends BaseCapability<T, C> {

    private static final Map<ResourceLocation, EntityCapability<?, ?>> REGISTRY = new ConcurrentHashMap<>();

    private final EntityApiLookup<T, C> lookup;

    @SuppressWarnings("unchecked")
    public static <T, C> EntityCapability<T, C> create(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        return (EntityCapability<T, C>) REGISTRY.computeIfAbsent(name, n -> new EntityCapability<>(n, typeClass, contextClass));
    }

    public static <T> EntityCapability<T, @Nullable Direction> createSided(ResourceLocation name, Class<T> typeClass) {
        return create(name, typeClass, Direction.class);
    }

    public static <T> EntityCapability<T, Void> createVoid(ResourceLocation name, Class<T> typeClass) {
        return create(name, typeClass, Void.class);
    }

    private EntityCapability(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        super(name, typeClass, contextClass);
        this.lookup = EntityApiLookup.get(name, typeClass, contextClass);
    }

    public EntityApiLookup<T, C> lookup() {
        return lookup;
    }

    @Nullable
    public T getCapability(Entity entity, C context) {
        return lookup.find(entity, context);
    }
}
