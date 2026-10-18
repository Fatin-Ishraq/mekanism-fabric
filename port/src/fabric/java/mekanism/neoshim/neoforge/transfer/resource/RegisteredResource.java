package mekanism.neoshim.neoforge.transfer.resource;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;

/** A resource whose type is a registry entry. */
public interface RegisteredResource<T> extends Resource, TypedInstance<T> {

    T value();

    default boolean is(Predicate<Holder<T>> predicate) {
        return predicate.test(typeHolder());
    }
}
