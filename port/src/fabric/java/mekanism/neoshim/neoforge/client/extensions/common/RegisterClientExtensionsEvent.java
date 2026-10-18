package mekanism.neoshim.neoforge.client.extensions.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import mekanism.neoshim.neoforge.fluids.FluidType;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class RegisterClientExtensionsEvent extends Event implements IModBusEvent {

    public static final Map<Block, IClientBlockExtensions> BLOCKS = new ConcurrentHashMap<>();
    public static final Map<Item, IClientItemExtensions> ITEMS = new ConcurrentHashMap<>();
    public static final Map<FluidType, IClientFluidTypeExtensions> FLUID_TYPES = new ConcurrentHashMap<>();

    public void registerBlock(IClientBlockExtensions extensions, Block... blocks) {
        for (Block block : blocks) {
            BLOCKS.put(block, extensions);
        }
    }

    @SafeVarargs
    public final void registerBlock(IClientBlockExtensions extensions, Holder<Block>... blocks) {
        for (Holder<Block> block : blocks) {
            BLOCKS.put(block.value(), extensions);
        }
    }

    public void registerItem(IClientItemExtensions extensions, Item... items) {
        for (Item item : items) {
            ITEMS.put(item, extensions);
        }
    }

    @SafeVarargs
    public final void registerItem(IClientItemExtensions extensions, Holder<Item>... items) {
        for (Holder<Item> item : items) {
            ITEMS.put(item.value(), extensions);
        }
    }

    public void registerFluidType(IClientFluidTypeExtensions extensions, FluidType... fluidTypes) {
        for (FluidType type : fluidTypes) {
            FLUID_TYPES.put(type, extensions);
        }
    }

    @SafeVarargs
    public final void registerFluidType(IClientFluidTypeExtensions extensions, Holder<FluidType>... fluidTypes) {
        for (Holder<FluidType> type : fluidTypes) {
            FLUID_TYPES.put(type.value(), extensions);
        }
    }

    public boolean isBlockRegistered(Block block) {
        return BLOCKS.containsKey(block);
    }

    public boolean isItemRegistered(Item item) {
        return ITEMS.containsKey(item);
    }
}
