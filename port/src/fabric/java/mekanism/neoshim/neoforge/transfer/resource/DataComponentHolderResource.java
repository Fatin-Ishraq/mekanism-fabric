package mekanism.neoshim.neoforge.transfer.resource;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

/** A registered resource that also carries data components (as a patch over its type's defaults). */
public interface DataComponentHolderResource<T> extends RegisteredResource<T>, DataComponentHolder {

    boolean isComponentsPatchEmpty();

    DataComponentHolderResource<T> withMergedPatch(DataComponentPatch patch);

    <D> DataComponentHolderResource<T> with(DataComponentType<D> type, D value);

    DataComponentHolderResource<T> without(DataComponentType<?> type);

    DataComponentPatch getComponentsPatch();

    default <D> DataComponentHolderResource<T> with(Supplier<? extends DataComponentType<D>> type, D value) {
        return with(type.get(), value);
    }

    default DataComponentHolderResource<T> without(Supplier<? extends DataComponentType<?>> type) {
        return without(type.get());
    }
}
