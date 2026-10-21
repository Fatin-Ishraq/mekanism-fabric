package mekanism.neoshim.neoforge.transfer.resource;

import java.util.function.Predicate;
import mekanism.neoshim.neoforge.registries.datamaps.DataMapType;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import org.jetbrains.annotations.Nullable;

/** A resource whose type is a registry entry. */
public interface RegisteredResource<T> extends Resource, TypedInstance<T> {

    T value();

    default boolean is(Predicate<Holder<T>> predicate) {
        return predicate.test(typeHolder());
    }

    /// Helper to look up a data map value for this resource's registry entry.
    @Nullable
    default <DATA> DATA getData(DataMapType<T, DATA> type) {
        return typeHolder().getData(type);
    }
}
