package mekanism.neoshim.neoforge.common.extensions;

import net.minecraft.world.item.CreativeModeTab;

/** Injected into {@link CreativeModeTab.Builder}. Search bars on non-search tabs are a NeoForge feature; no-op on Fabric. */
public interface ICreativeModeTabBuilderExtension {

    default CreativeModeTab.Builder withSearchBar() {
        return (CreativeModeTab.Builder) this;
    }

    default CreativeModeTab.Builder withSearchBar(int searchBarWidth) {
        return (CreativeModeTab.Builder) this;
    }

    /** Tab ordering hints: Fabric orders modded tabs by registration (Mekanism core registers first), so these are no-ops. */
    default CreativeModeTab.Builder withTabsBefore(net.minecraft.resources.ResourceLocation... tabs) {
        return (CreativeModeTab.Builder) this;
    }

    @SuppressWarnings("unchecked")
    default CreativeModeTab.Builder withTabsBefore(net.minecraft.resources.ResourceKey<CreativeModeTab>... tabs) {
        return (CreativeModeTab.Builder) this;
    }

    default CreativeModeTab.Builder withTabsAfter(net.minecraft.resources.ResourceLocation... tabs) {
        return (CreativeModeTab.Builder) this;
    }

    @SuppressWarnings("unchecked")
    default CreativeModeTab.Builder withTabsAfter(net.minecraft.resources.ResourceKey<CreativeModeTab>... tabs) {
        return (CreativeModeTab.Builder) this;
    }
}
