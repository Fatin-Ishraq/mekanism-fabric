package mekanism.neoshim.neoforge.event;

/** Fired on the mod bus when server data reload listeners are collected (NeoForge fires it per reload; the bridge collects once at init). */
public class AddServerReloadListenersEvent extends SortedReloadListenerEvent {

    public AddServerReloadListenersEvent() {
    }
}
