package mekanism.neoshim.neoforge.client.event;

import java.util.ArrayList;
import java.util.List;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import mekanism.neoshim.neoforge.client.gui.GuiLayer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/** Collected by the client bootstrap; layers are attached to Fabric's HUD element registry relative to their anchors. */
public class RegisterGuiLayersEvent extends Event implements IModBusEvent {

    public enum Placement {
        BELOW_ALL,
        BELOW,
        ABOVE,
        ABOVE_ALL
    }

    public record Entry(Identifier id, GuiLayer layer, @Nullable Identifier anchor, Placement placement) {
    }

    private final List<Entry> layers = new ArrayList<>();

    public void registerBelowAll(Identifier id, GuiLayer layer) {
        register(Placement.BELOW_ALL, null, id, layer);
    }

    public void registerBelow(Identifier other, Identifier id, GuiLayer layer) {
        register(Placement.BELOW, other, id, layer);
    }

    public void registerAbove(Identifier other, Identifier id, GuiLayer layer) {
        register(Placement.ABOVE, other, id, layer);
    }

    public void registerAboveAll(Identifier id, GuiLayer layer) {
        register(Placement.ABOVE_ALL, null, id, layer);
    }

    private void register(Placement placement, @Nullable Identifier anchor, Identifier id, GuiLayer layer) {
        for (Entry entry : layers) {
            if (entry.id().equals(id)) {
                throw new IllegalArgumentException("Duplicate GUI layer: " + id);
            }
        }
        layers.add(new Entry(id, layer, anchor, placement));
    }

    public List<Entry> getLayers() {
        return layers;
    }
}
