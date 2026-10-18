package mekanism.neoshim.neoforge.client.event;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.client.renderer.texture.TextureAtlas;

public class TextureAtlasStitchedEvent extends Event implements IModBusEvent {

    private final TextureAtlas atlas;

    public TextureAtlasStitchedEvent(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }
}
