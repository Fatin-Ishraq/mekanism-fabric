package mekanism.neoshim.neoforge.common.extensions;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link DataComponentGetter}; adds NeoForge's supplier overloads for deferred component types. */
public interface IDataComponentGetterExtension {

    private DataComponentGetter self() {
        return (DataComponentGetter) this;
    }

    @Nullable
    default <T> T get(Supplier<? extends DataComponentType<? extends T>> componentType) {
        return self().get(componentType.get());
    }

    default <T> T getOrDefault(Supplier<? extends DataComponentType<? extends T>> componentType, T value) {
        return self().getOrDefault(componentType.get(), value);
    }

    default <T> boolean has(Supplier<? extends DataComponentType<? extends T>> componentType) {
        return get(componentType) != null;
    }

    default boolean has(DataComponentType<?> componentType) {
        return self().get(componentType) != null;
    }
}
