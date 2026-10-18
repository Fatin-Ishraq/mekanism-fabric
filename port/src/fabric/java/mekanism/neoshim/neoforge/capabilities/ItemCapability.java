package mekanism.neoshim.neoforge.capabilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Item capability backed by a Fabric {@link ItemApiLookup} with the same id. */
public final class ItemCapability<T, C> extends BaseCapability<T, C> {

    private static final Map<Identifier, ItemCapability<?, ?>> REGISTRY = new ConcurrentHashMap<>();

    private final ItemApiLookup<T, C> lookup;

    @SuppressWarnings("unchecked")
    public static <T, C> ItemCapability<T, C> create(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        return (ItemCapability<T, C>) REGISTRY.computeIfAbsent(name, n -> new ItemCapability<>(n, typeClass, contextClass));
    }

    public static <T> ItemCapability<T, Void> createVoid(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, Void.class);
    }

    private ItemCapability(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        super(name, typeClass, contextClass);
        this.lookup = ItemApiLookup.get(name, typeClass, contextClass);
    }

    public ItemApiLookup<T, C> lookup() {
        return lookup;
    }

    @Nullable
    public T getCapability(ItemStack stack, C context) {
        return stack.isEmpty() ? null : lookup.find(stack, context);
    }
}
