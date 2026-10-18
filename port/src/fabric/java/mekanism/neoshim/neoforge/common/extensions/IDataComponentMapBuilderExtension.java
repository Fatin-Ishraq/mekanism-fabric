package mekanism.neoshim.neoforge.common.extensions;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link DataComponentMap.Builder}. */
public interface IDataComponentMapBuilderExtension {

    default <T> DataComponentMap.Builder set(Supplier<? extends DataComponentType<T>> type, @Nullable T value) {
        return ((DataComponentMap.Builder) this).set(type.get(), value);
    }
}
