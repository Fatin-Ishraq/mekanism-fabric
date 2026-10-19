package mekanism.neoshim.neoforge.event;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/** Wraps Fabric's {@link FabricCreativeModeTabOutput} (fired per tab from CreativeModeTabEvents.MODIFY_OUTPUT_ALL). */
public class BuildCreativeModeTabContentsEvent extends Event implements IModBusEvent, CreativeModeTab.Output {

    private final CreativeModeTab tab;
    private final ResourceKey<CreativeModeTab> tabKey;
    private final FabricCreativeModeTabOutput entries;

    public BuildCreativeModeTabContentsEvent(CreativeModeTab tab, ResourceKey<CreativeModeTab> tabKey, FabricCreativeModeTabOutput entries) {
        this.tab = tab;
        this.tabKey = tabKey;
        this.entries = entries;
    }

    public CreativeModeTab getTab() {
        return tab;
    }

    public ResourceKey<CreativeModeTab> getTabKey() {
        return tabKey;
    }

    public FeatureFlagSet getFlags() {
        return entries.getEnabledFeatures();
    }

    public CreativeModeTab.ItemDisplayParameters getParameters() {
        return entries.getContext();
    }

    public boolean hasPermissions() {
        return entries.shouldShowOpRestrictedItems();
    }

    @Override
    public void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
        entries.accept(stack, visibility);
    }

    public void insertAfter(ItemStack existingEntry, ItemStack newEntry, CreativeModeTab.TabVisibility visibility) {
        entries.insertAfter(existingEntry, java.util.List.of(newEntry), visibility);
    }

    public void insertBefore(ItemStack existingEntry, ItemStack newEntry, CreativeModeTab.TabVisibility visibility) {
        entries.insertBefore(existingEntry, java.util.List.of(newEntry), visibility);
    }

    public void insertFirst(ItemStack newEntry, CreativeModeTab.TabVisibility visibility) {
        entries.prepend(newEntry, visibility);
    }

    public void remove(ItemStack existingEntry, CreativeModeTab.TabVisibility visibility) {
        removeIf(stack -> ItemStack.isSameItemSameComponents(stack, existingEntry), visibility);
    }

    public void removeIf(java.util.function.Predicate<? super ItemStack> filter, CreativeModeTab.TabVisibility visibility) {
        if (visibility != CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY) {
            entries.getDisplayStacks().removeIf(filter);
        }
        if (visibility != CreativeModeTab.TabVisibility.PARENT_TAB_ONLY) {
            entries.getSearchTabStacks().removeIf(filter);
        }
    }

    public void acceptAll(java.util.Collection<ItemStack> stacks) {
        stacks.forEach(this::accept);
    }

    public void accept(ItemLike item) {
        accept(new ItemStack(item));
    }
}
