package mekanism.neoshim.neoforge.client.event;

import java.util.ArrayList;
import java.util.List;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.server.packs.resources.PreparableReloadListener;

/** Collected by the client bootstrap and registered with Fabric's ResourceManagerHelper (CLIENT_RESOURCES). */
public class RegisterClientReloadListenersEvent extends Event implements IModBusEvent {

    private final List<PreparableReloadListener> listeners = new ArrayList<>();

    public void registerReloadListener(PreparableReloadListener reloadListener) {
        listeners.add(reloadListener);
    }

    public List<PreparableReloadListener> getListeners() {
        return listeners;
    }
}
