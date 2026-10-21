package mekanism.neoshim.neoforge.event;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mekanism.neoshim.bus.api.Event;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

/**
 * Reload listeners are named in NeoForge so they can be ordered against each other. Fabric orders listeners through
 * {@code IdentifiableResourceReloadListener#getFabricDependencies}, so dependencies are collected but only honoured by the bridge where possible.
 */
public abstract class SortedReloadListenerEvent extends Event {

    private final Map<Identifier, PreparableReloadListener> registry = new LinkedHashMap<>();

    public void addListener(Identifier id, PreparableReloadListener listener) {
        registry.put(id, listener);
    }

    public void addDependency(Identifier id, Identifier dependency) {
    }

    public Map<Identifier, PreparableReloadListener> getRegistry() {
        return registry;
    }

    public List<PreparableReloadListener> getListeners() {
        return new ArrayList<>(registry.values());
    }
}
