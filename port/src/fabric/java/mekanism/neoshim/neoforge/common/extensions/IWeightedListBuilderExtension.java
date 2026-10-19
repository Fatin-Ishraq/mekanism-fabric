package mekanism.neoshim.neoforge.common.extensions;

import java.util.List;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;

/** Injected into {@link WeightedList.Builder}: the read access NeoForge patches in (used by biome modifiers). */
public interface IWeightedListBuilderExtension<E> {

    @SuppressWarnings("unchecked")
    private WeightedList.Builder<E> self() {
        return (WeightedList.Builder<E>) (Object) this;
    }

    default List<Weighted<E>> getList() {
        return self().result.build();
    }

    default WeightedList.Builder<E> add(Weighted<E> weighted) {
        return self().add(weighted.value(), weighted.weight());
    }
}
