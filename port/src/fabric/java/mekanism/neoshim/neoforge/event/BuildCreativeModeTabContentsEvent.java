package mekanism.neoshim.neoforge.event;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/** Wraps Fabric's {@link FabricItemGroupEntries} (fired per tab from ItemGroupEvents.MODIFY_ENTRIES_ALL). */
public class BuildCreativeModeTabContentsEvent extends Event implements IModBusEvent, CreativeModeTab.Output {

    private final CreativeModeTab tab;
    private final ResourceKey<CreativeModeTab> tabKey;
    private final FabricItemGroupEntries entries;

    public BuildCreativeModeTabContentsEvent(CreativeModeTab tab, ResourceKey<CreativeModeTab> tabKey, FabricItemGroupEntries entries) {
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
        entries.addAfter(existingEntry, java.util.List.of(newEntry), visibility);
    }

    public void insertBefore(ItemStack existingEntry, ItemStack newEntry, CreativeModeTab.TabVisibility visibility) {
        entries.addBefore(existingEntry, java.util.List.of(newEntry), visibility);
    }

    public void acceptAll(java.util.Collection<ItemStack> stacks) {
        stacks.forEach(this::accept);
    }

    public void accept(ItemLike item) {
        accept(new ItemStack(item));
    }
}
