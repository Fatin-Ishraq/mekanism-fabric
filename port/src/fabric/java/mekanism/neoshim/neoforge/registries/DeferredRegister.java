package mekanism.neoshim.neoforge.registries;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.neoshim.bus.api.IEventBus;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * Same contract as NeoForge's DeferredRegister: entries are collected, and registered into the registry when the {@link RegisterEvent} for
 * that registry is fired on the mod bus passed to {@link #register(IEventBus)}. The Fabric bootstrap fires RegisterEvents in
 * {@code BuiltInRegistries.REGISTRY} order (NOTES.md D3).
 */
public class DeferredRegister<T> {

    private final ResourceKey<? extends Registry<T>> registryKey;
    private final String namespace;
    private final Map<DeferredHolder<T, ? extends T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
    private final Set<DeferredHolder<T, ? extends T>> entriesView = Collections.unmodifiableSet(entries.keySet());
    private boolean seenRegisterEvent = false;
    private boolean registeredEventBus = false;

    public static <T> DeferredRegister<T> create(Registry<T> registry, String namespace) {
        return new DeferredRegister<>(registry.key(), namespace);
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String namespace) {
        return new DeferredRegister<>(key, namespace);
    }

    public static <B> DeferredRegister<B> create(ResourceLocation registryName, String modid) {
        return new DeferredRegister<>(ResourceKey.createRegistryKey(registryName), modid);
    }

    public static DeferredRegister<Item> createItems(String modid) {
        return create(Registries.ITEM, modid);
    }

    public static DeferredRegister<Block> createBlocks(String modid) {
        return create(Registries.BLOCK, modid);
    }

    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        this.registryKey = Objects.requireNonNull(registryKey);
        this.namespace = Objects.requireNonNull(namespace);
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> sup) {
        return this.register(name, key -> sup.get());
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Function<ResourceLocation, ? extends I> func) {
        if (seenRegisterEvent) {
            throw new IllegalStateException("Cannot register new entries to DeferredRegister after RegisterEvent has been fired.");
        }
        Objects.requireNonNull(name);
        Objects.requireNonNull(func);
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath(namespace, name);
        DeferredHolder<T, I> ret = createHolder(this.registryKey, key);
        if (entries.putIfAbsent(ret, () -> func.apply(key)) != null) {
            throw new IllegalArgumentException("Duplicate registration " + name);
        }
        return ret;
    }

    protected <I extends T> DeferredHolder<T, I> createHolder(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation key) {
        return DeferredHolder.create(registryKey, key);
    }

    public TagKey<T> createTagKey(String path) {
        return createTagKey(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public TagKey<T> createTagKey(ResourceLocation location) {
        return TagKey.create(this.registryKey, location);
    }

    public void register(IEventBus bus) {
        if (this.registeredEventBus) {
            throw new IllegalStateException("Cannot register DeferredRegister to more than one event bus.");
        }
        this.registeredEventBus = true;
        bus.addListener(RegisterEvent.class, this::addEntries);
    }

    public Collection<DeferredHolder<T, ? extends T>> getEntries() {
        return entriesView;
    }

    public ResourceKey<? extends Registry<T>> getRegistryKey() {
        return this.registryKey;
    }

    public ResourceLocation getRegistryName() {
        return this.registryKey.location();
    }

    public String getNamespace() {
        return this.namespace;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public Registry<T> getRegistry() {
        return (Registry<T>) net.minecraft.core.registries.BuiltInRegistries.REGISTRY.get(this.registryKey.location());
    }

    private void addEntries(RegisterEvent event) {
        if (event.getRegistryKey().equals(this.registryKey)) {
            this.seenRegisterEvent = true;
            for (Map.Entry<DeferredHolder<T, ? extends T>, Supplier<? extends T>> e : entries.entrySet()) {
                event.register(this.registryKey, e.getKey().getId(), () -> e.getValue().get());
                e.getKey().bind(false);
            }
        }
    }

    /** Helper for subclasses that need the vanilla holder type. */
    protected static <T> Holder<T> asHolder(DeferredHolder<T, ?> holder) {
        return holder;
    }
}
