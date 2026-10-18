package mekanism.neoshim.neoforge.common;

import mekanism.neoshim.bus.EventBusImpl;
import mekanism.neoshim.bus.api.IEventBus;

public class NeoForge {

    /** The game event bus. Fabric callbacks are bridged onto it by {@code mekanism.fabric.bridge}. */
    public static final IEventBus EVENT_BUS = new EventBusImpl("game bus");
}
