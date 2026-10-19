package mekanism.neoshim.neoforge.client.event;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.bus.api.ICancellableEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import mekanism.neoshim.neoforge.client.gui.GuiLayer;
import net.minecraft.resources.Identifier;

public abstract class RenderGuiLayerEvent extends Event {

    private final GuiGraphicsExtractor guiGraphics;
    private final DeltaTracker partialTick;
    private final Identifier name;
    private final GuiLayer layer;

    protected RenderGuiLayerEvent(GuiGraphicsExtractor guiGraphics, DeltaTracker partialTick, Identifier name, GuiLayer layer) {
        this.guiGraphics = guiGraphics;
        this.partialTick = partialTick;
        this.name = name;
        this.layer = layer;
    }

    public GuiGraphicsExtractor getGuiGraphics() {
        return guiGraphics;
    }

    public DeltaTracker getPartialTick() {
        return partialTick;
    }

    public Identifier getName() {
        return name;
    }

    public GuiLayer getLayer() {
        return layer;
    }

    public static class Pre extends RenderGuiLayerEvent implements ICancellableEvent {

        public Pre(GuiGraphicsExtractor guiGraphics, DeltaTracker partialTick, Identifier name, GuiLayer layer) {
            super(guiGraphics, partialTick, name, layer);
        }
    }

    public static class Post extends RenderGuiLayerEvent {

        public Post(GuiGraphicsExtractor guiGraphics, DeltaTracker partialTick, Identifier name, GuiLayer layer) {
            super(guiGraphics, partialTick, name, layer);
        }
    }
}
