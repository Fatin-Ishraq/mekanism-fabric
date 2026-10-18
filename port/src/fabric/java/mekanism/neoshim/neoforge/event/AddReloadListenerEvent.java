package mekanism.neoshim.neoforge.event;

import java.util.ArrayList;
import java.util.List;
import mekanism.neoshim.bus.api.Event;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.PreparableReloadListener;

/**
 * Collected once at init by the bridge; listeners are registered with Fabric's ResourceManagerHelper (SERVER_DATA) and ordered after
 * vanilla recipes/tags (NOTES.md D2).
 */
public class AddReloadListenerEvent extends Event {

    private final List<PreparableReloadListener> listeners = new ArrayList<>();

    public void addListener(PreparableReloadListener listener) {
        listeners.add(listener);
    }

    public List<PreparableReloadListener> getListeners() {
        return listeners;
    }

    public RegistryAccess getRegistryAccess() {
        throw new UnsupportedOperationException("Registry access is not known when reload listeners are registered on Fabric");
    }
}
