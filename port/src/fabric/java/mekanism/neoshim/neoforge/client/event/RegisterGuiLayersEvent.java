package mekanism.neoshim.neoforge.client.event;

import java.util.ArrayList;
import java.util.List;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.Identifier;

/** Collected by the client bootstrap; layers are drawn from Fabric's HudRenderCallback in registration order. */
public class RegisterGuiLayersEvent extends Event implements IModBusEvent {

    public record Entry(Identifier id, LayeredDraw.Layer layer) {
    }

    private final List<Entry> layers = new ArrayList<>();

    public void registerBelowAll(Identifier id, LayeredDraw.Layer layer) {
        layers.addFirst(new Entry(id, layer));
    }

    public void registerBelow(Identifier other, Identifier id, LayeredDraw.Layer layer) {
        layers.add(new Entry(id, layer));
    }

    public void registerAbove(Identifier other, Identifier id, LayeredDraw.Layer layer) {
        layers.add(new Entry(id, layer));
    }

    public void registerAboveAll(Identifier id, LayeredDraw.Layer layer) {
        layers.add(new Entry(id, layer));
    }

    public List<Entry> getLayers() {
        return layers;
    }
}
