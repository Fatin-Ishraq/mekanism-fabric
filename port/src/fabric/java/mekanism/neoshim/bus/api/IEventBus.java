package mekanism.neoshim.bus.api;

import java.util.function.Consumer;

public interface IEventBus {

    /**
     * Registers all {@link SubscribeEvent} methods: static methods if {@code target} is a {@link Class}, otherwise instance methods.
     */
    void register(Object target);

    void unregister(Object target);

    <T extends Event> void addListener(Consumer<T> listener);

    <T extends Event> void addListener(EventPriority priority, Consumer<T> listener);

    <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Consumer<T> listener);

    <T extends Event> void addListener(Class<T> eventType, Consumer<T> listener);

    <T extends Event> void addListener(EventPriority priority, Class<T> eventType, Consumer<T> listener);

    <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Class<T> eventType, Consumer<T> listener);

    <T extends Event> void addListener(boolean receiveCanceled, Consumer<T> listener);

    <T extends Event> T post(T event);
}
