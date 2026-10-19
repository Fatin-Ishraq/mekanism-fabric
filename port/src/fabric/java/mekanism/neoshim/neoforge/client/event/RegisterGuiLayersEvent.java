package mekanism.neoshim.neoforge.client.event;

import java.util.ArrayList;
import java.util.List;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import mekanism.neoshim.neoforge.client.gui.GuiLayer;
import net.minecraft.resources.Identifier;

/** Collected by the client bootstrap; layers are drawn from Fabric's HudRenderCallback in registration order. */
public class RegisterGuiLayersEvent extends Event implements IModBusEvent {

    public record Entry(Identifier id, GuiLayer layer) {
    }

    private final List<Entry> layers = new ArrayList<>();

    public void registerBelowAll(Identifier id, GuiLayer layer) {
        layers.addFirst(new Entry(id, layer));
    }

    public void registerBelow(Identifier other, Identifier id, GuiLayer layer) {
        layers.add(new Entry(id, layer));
    }

    public void registerAbove(Identifier other, Identifier id, GuiLayer layer) {
        layers.add(new Entry(id, layer));
    }

    public void registerAboveAll(Identifier id, GuiLayer layer) {
        layers.add(new Entry(id, layer));
    }

    public void replaceLayer(Identifier id, GuiLayer layer) {
        layers.replaceAll(entry -> entry.id().equals(id) ? new Entry(id, layer) : entry);
    }

    public void wrapLayer(Identifier id, java.util.function.UnaryOperator<GuiLayer> wrapper) {
        layers.replaceAll(entry -> entry.id().equals(id) ? new Entry(id, wrapper.apply(entry.layer())) : entry);
    }

    public List<Entry> getLayers() {
        return layers;
    }
}
