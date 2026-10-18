package mekanism.neoshim.neoforge.client.event;

import mekanism.neoshim.bus.api.Event;

public abstract class ClientTickEvent extends Event {

    public static class Pre extends ClientTickEvent {
    }

    public static class Post extends ClientTickEvent {
    }
}
