package mekanism.neoshim.neoforge.client.gui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** A HUD layer, drawn through Fabric's HUD element registry by the client bridge. */
@FunctionalInterface
public interface GuiLayer {

    void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker);
}
