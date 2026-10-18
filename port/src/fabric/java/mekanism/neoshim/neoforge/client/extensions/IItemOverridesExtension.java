package mekanism.neoshim.neoforge.client.extensions;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.ItemOverrides;

/** Injected into {@link ItemOverrides}. */
public interface IItemOverridesExtension {

    default ImmutableList<ItemOverrides.BakedOverride> getOverrides() {
        return ImmutableList.copyOf(((ItemOverrides) this).overrides);
    }
}
