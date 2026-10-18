package mekanism.neoshim.neoforge.common.extensions;

import net.minecraft.world.item.CreativeModeTab;

/** Injected into {@link CreativeModeTab.Builder}. Search bars on non-search tabs are a NeoForge feature; no-op on Fabric. */
public interface ICreativeModeTabBuilderExtension {

    default CreativeModeTab.Builder withSearchBar() {
        return (CreativeModeTab.Builder) this;
    }
}
