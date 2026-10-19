package mekanism.neoshim.neoforge.common.extensions;

import java.util.Map;
import mekanism.fabric.datamaps.DataMapLoader;
import mekanism.neoshim.neoforge.registries.datamaps.DataMapType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link Registry}. */
public interface IRegistryExtension<T> {

    private Registry<T> self() {
        return (Registry<T>) this;
    }

    /** Like getKey, but never falls back to a defaulted registry's default key. */
    @Nullable
    default Identifier getKeyOrNull(T element) {
        return self().getResourceKey(element).map(ResourceKey::identifier).orElse(null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default <A> Map<ResourceKey<T>, A> getDataMap(DataMapType<T, A> type) {
        return (Map) DataMapLoader.values().getOrDefault(type, Map.of());
    }

    @Nullable
    default <A> A getData(DataMapType<T, A> type, ResourceKey<T> key) {
        return self().get(key).map(holder -> DataMapLoader.getData(type, (Holder<T>) holder)).orElse(null);
    }

    default boolean containsValue(T value) {
        return self().getResourceKey(value).isPresent();
    }
}
