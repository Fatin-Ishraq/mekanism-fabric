package mekanism.neoshim.neoforge.client.event;

import mekanism.neoshim.fml.event.IModBusEvent;
import mekanism.neoshim.neoforge.event.SortedReloadListenerEvent;

/** Fired on the mod bus while the client resource manager is being prepared; listeners are registered with Fabric's client resource manager. */
public class AddClientReloadListenersEvent extends SortedReloadListenerEvent implements IModBusEvent {

    public AddClientReloadListenersEvent() {
    }
}
