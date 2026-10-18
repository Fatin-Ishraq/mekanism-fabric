package mekanism.neoshim.neoforge.network.event;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import mekanism.neoshim.neoforge.network.registration.PayloadRegistrar;

public class RegisterPayloadHandlersEvent extends Event implements IModBusEvent {

    public PayloadRegistrar registrar(String version) {
        return new PayloadRegistrar(version);
    }
}
