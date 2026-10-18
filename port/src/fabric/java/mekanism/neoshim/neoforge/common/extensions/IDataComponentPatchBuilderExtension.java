package mekanism.neoshim.neoforge.common.extensions;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

/** Injected into {@link DataComponentPatch.Builder}. */
public interface IDataComponentPatchBuilderExtension {

    default <T> DataComponentPatch.Builder set(Supplier<? extends DataComponentType<T>> type, T value) {
        return ((DataComponentPatch.Builder) this).set(type.get(), value);
    }

    default <T> DataComponentPatch.Builder remove(Supplier<? extends DataComponentType<T>> type) {
        return ((DataComponentPatch.Builder) this).remove(type.get());
    }
}
