package mekanism.neoshim.neoforge.registries;

import com.mojang.datafixers.util.Either;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Lazily bound reference to a registry entry, with the same semantics as NeoForge's DeferredHolder: it implements {@link Holder} by delegating
 * to the registry's own holder once the entry is registered.
 */
public class DeferredHolder<R, T extends R> implements Holder<R>, Supplier<T> {

    protected final ResourceKey<R> key;
    @Nullable
    private Holder<R> holder = null;

    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<? extends Registry<R>> registryKey, Identifier valueName) {
        return create(ResourceKey.create(registryKey, valueName));
    }

    public static <R, T extends R> DeferredHolder<R, T> create(Identifier registryName, Identifier valueName) {
        return create(ResourceKey.createRegistryKey(registryName), valueName);
    }

    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<R> key) {
        return new DeferredHolder<>(key);
    }

    protected DeferredHolder(ResourceKey<R> key) {
        this.key = Objects.requireNonNull(key);
        this.bind(false);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public T value() {
        bind(true);
        if (this.holder == null) {
            throw new NullPointerException("Trying to access unbound value: " + this.key);
        }
        return (T) this.holder.value();
    }

    @NotNull
    @Override
    public T get() {
        return value();
    }

    public Optional<T> asOptional() {
        return isBound() ? Optional.of(value()) : Optional.empty();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected Registry<R> getRegistry() {
        return (Registry<R>) BuiltInRegistries.REGISTRY.getValue(this.key.registry());
    }

    @SuppressWarnings("unchecked")
    protected final void bind(boolean throwOnMissingRegistry) {
        if (this.holder != null) {
            return;
        }
        Registry<R> registry = getRegistry();
        if (registry != null) {
            this.holder = registry.get(this.key).orElse(null);
        } else if (throwOnMissingRegistry) {
            throw new IllegalStateException("Registry not present for " + this + ": " + this.key.registry());
        }
    }

    public Identifier getId() {
        return this.key.identifier();
    }

    public ResourceKey<R> getKey() {
        return this.key;
    }

    @Override
    public boolean isBound() {
        bind(false);
        return this.holder != null && this.holder.isBound();
    }

    @Override
    public boolean areComponentsBound() {
        bind(false);
        return this.holder != null && this.holder.areComponentsBound();
    }

    @Override
    public net.minecraft.core.component.DataComponentMap components() {
        bind(true);
        return this.holder != null ? this.holder.components() : net.minecraft.core.component.DataComponentMap.EMPTY;
    }

    @Nullable
    public <Z> Z getData(mekanism.neoshim.neoforge.registries.datamaps.DataMapType<R, Z> type) {
        bind(false);
        return this.holder != null ? this.holder.getData(type) : null;
    }

    @Override
    public boolean is(Identifier id) {
        return id.equals(this.key.identifier());
    }

    @Override
    public boolean is(ResourceKey<R> resourceKey) {
        return resourceKey == this.key;
    }

    @Override
    public boolean is(Predicate<ResourceKey<R>> filter) {
        return filter.test(this.key);
    }

    @Override
    public boolean is(TagKey<R> tag) {
        bind(false);
        return this.holder != null && this.holder.is(tag);
    }

    @Override
    @Deprecated
    public boolean is(Holder<R> holder) {
        bind(false);
        return this.holder != null && this.holder.is(holder);
    }

    @Override
    public Stream<TagKey<R>> tags() {
        bind(false);
        return this.holder != null ? this.holder.tags() : Stream.empty();
    }

    @Override
    public Either<ResourceKey<R>, R> unwrap() {
        return Either.left(this.key);
    }

    @Override
    public Optional<ResourceKey<R>> unwrapKey() {
        return Optional.of(this.key);
    }

    @Override
    public Kind kind() {
        return Kind.REFERENCE;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<R> owner) {
        bind(false);
        return this.holder != null && this.holder.canSerializeIn(owner);
    }

    /** The bound registry holder (NeoForge {@code getDelegate}). */
    public Holder<R> getDelegate() {
        bind(true);
        return this.holder != null ? this.holder : this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof Holder<?> h && h.kind() == Kind.REFERENCE && h.unwrapKey().map(k -> k == this.key).orElse(false);
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.ENGLISH, "DeferredHolder{%s}", this.key);
    }
}
