package mekanism.neoshim.neoforge.client.event.sound;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.client.sounds.SoundEngine;

public class SoundEngineLoadEvent extends Event implements IModBusEvent {

    private final SoundEngine engine;

    public SoundEngineLoadEvent(SoundEngine engine) {
        this.engine = engine;
    }

    public SoundEngine getEngine() {
        return engine;
    }
}
