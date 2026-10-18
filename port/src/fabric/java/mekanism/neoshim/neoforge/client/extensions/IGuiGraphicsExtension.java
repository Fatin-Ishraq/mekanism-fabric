package mekanism.neoshim.neoforge.client.extensions;

import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/** Injected into {@link GuiGraphics}. */
public interface IGuiGraphicsExtension {

    default int drawString(Font font, net.minecraft.util.FormattedCharSequence text, float x, float y, int color, boolean dropShadow) {
        GuiGraphics self = (GuiGraphics) this;
        int width = font.drawInBatch(text, x, y, color, dropShadow, self.pose().last().pose(), self.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
        self.flush();
        return width;
    }

    default int drawString(Font font, String text, float x, float y, int color, boolean dropShadow) {
        GuiGraphics self = (GuiGraphics) this;
        int width = font.drawInBatch(text, x, y, color, dropShadow, self.pose().last().pose(), self.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
        self.flush();
        return width;
    }

    default void renderTooltip(Font font, List<Component> textComponents, Optional<TooltipComponent> tooltipComponent, ItemStack stack, int mouseX, int mouseY) {
        ((GuiGraphics) this).renderTooltip(font, textComponents, tooltipComponent, mouseX, mouseY);
    }
}
