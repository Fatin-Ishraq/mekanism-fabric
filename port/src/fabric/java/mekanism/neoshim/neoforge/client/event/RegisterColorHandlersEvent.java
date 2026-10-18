package mekanism.neoshim.neoforge.client.event;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.level.ItemLike;

public abstract class RegisterColorHandlersEvent extends Event implements IModBusEvent {

    public static class Block extends RegisterColorHandlersEvent {

        private final BlockColors blockColors;

        public Block(BlockColors blockColors) {
            this.blockColors = blockColors;
        }

        public BlockColors getBlockColors() {
            return blockColors;
        }

        public void register(BlockColor blockColor, net.minecraft.world.level.block.Block... blocks) {
            ColorProviderRegistry.BLOCK.register(blockColor, blocks);
        }
    }

    public static class Item extends RegisterColorHandlersEvent {

        private final ItemColors itemColors;
        private final BlockColors blockColors;

        public Item(ItemColors itemColors, BlockColors blockColors) {
            this.itemColors = itemColors;
            this.blockColors = blockColors;
        }

        public ItemColors getItemColors() {
            return itemColors;
        }

        public BlockColors getBlockColors() {
            return blockColors;
        }

        public void register(ItemColor itemColor, ItemLike... items) {
            ColorProviderRegistry.ITEM.register(itemColor, items);
        }
    }
}
