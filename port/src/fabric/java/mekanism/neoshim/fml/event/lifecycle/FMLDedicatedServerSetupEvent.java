package mekanism.neoshim.fml.event.lifecycle;

import mekanism.neoshim.fml.ModContainer;

public class FMLDedicatedServerSetupEvent extends ModLifecycleEvent {

    public FMLDedicatedServerSetupEvent(ModContainer container) {
        super(container);
    }
}
