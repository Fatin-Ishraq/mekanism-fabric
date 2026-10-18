package mekanism.fabric.datamaps;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.neoshim.neoforge.registries.datamaps.DataMapType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

/** Registry of data map types and their loaded values. TODO(phase 5): JSON loading reload listener + client sync. */
public final class DataMapLoader {

    private static final Map<DataMapType<?, ?>, Map<ResourceKey<?>, Object>> VALUES = new ConcurrentHashMap<>();

    private DataMapLoader() {
    }

    public static void register(DataMapType<?, ?> type) {
        VALUES.putIfAbsent(type, new ConcurrentHashMap<>());
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <R, T> T getData(DataMapType<R, T> type, Holder<R> holder) {
        Map<ResourceKey<?>, Object> values = VALUES.get(type);
        if (values == null) {
            return null;
        }
        return holder.unwrapKey().map(key -> (T) values.get(key)).orElse(null);
    }

    public static Map<DataMapType<?, ?>, Map<ResourceKey<?>, Object>> values() {
        return VALUES;
    }
}
