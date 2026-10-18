package mekanism.neoshim.neoforge.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface IItemDecorator {

    boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset);
}
