package mekanism.neoshim.neoforge.common.extensions;

import java.util.List;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;

/**
 * Injected into {@link WeightedList.Builder}. NeoForge adds weighted-entry access to the builder, which biome modifiers rely on.
 */
public interface IWeightedListBuilderExtension<E> {

    @SuppressWarnings("unchecked")
    default WeightedList.Builder<E> add(Weighted<E> weighted) {
        return ((WeightedList.Builder<E>) this).add(weighted.value(), weighted.weight());
    }

    /// @return the entries added so far
    List<Weighted<E>> getList();
}
