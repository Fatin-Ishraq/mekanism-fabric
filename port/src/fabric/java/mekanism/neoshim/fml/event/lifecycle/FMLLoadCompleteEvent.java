package mekanism.neoshim.fml.event.lifecycle;

import mekanism.neoshim.fml.ModContainer;

public class FMLLoadCompleteEvent extends ModLifecycleEvent {

    public FMLLoadCompleteEvent(ModContainer container) {
        super(container);
    }
}
