package mekanism.neoshim.bus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.bus.api.EventPriority;
import mekanism.neoshim.bus.api.ICancellableEvent;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.bus.api.SubscribeEvent;
import net.jodah.typetools.TypeResolver;

/**
 * Minimal event bus with NeoForge semantics: listeners receive events of their declared type and its subtypes, ordered by priority, and
 * cancelled events skip listeners that do not opt into receiving cancelled events.
 * <p>
 * The listener's event type is resolved with TypeTools when it is not given explicitly. If that fails (it can on some JVMs for lambdas),
 * the listener is registered as "untyped" and its type is learned on first dispatch by catching the {@link ClassCastException} raised by
 * the lambda's own bridge method before any user code runs.
 */
public class EventBusImpl implements IEventBus {

    private final String name;
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private final Map<Class<?>, List<Listener>> dispatchCache = new ConcurrentHashMap<>();
    private final Map<Object, List<Listener>> registeredTargets = new ConcurrentHashMap<>();

    public EventBusImpl(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "EventBus[" + name + "]";
    }

    @Override
    public void register(Object target) {
        List<Listener> added = new ArrayList<>();
        boolean isStatic = target instanceof Class<?>;
        Class<?> clazz = isStatic ? (Class<?>) target : target.getClass();
        for (Method method : collectMethods(clazz)) {
            SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
            if (annotation == null || Modifier.isStatic(method.getModifiers()) != isStatic) {
                continue;
            }
            if (method.getParameterCount() != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                throw new IllegalArgumentException("Invalid @SubscribeEvent method " + method);
            }
            method.setAccessible(true);
            MethodHandle handle;
            try {
                handle = MethodHandles.lookup().unreflect(method);
                if (!isStatic) {
                    handle = handle.bindTo(target);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot access @SubscribeEvent method " + method, e);
            }
            MethodHandle finalHandle = handle;
            Consumer<Object> consumer = event -> {
                try {
                    finalHandle.invoke(event);
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            };
            added.add(addInternal(annotation.priority(), annotation.receiveCanceled(), method.getParameterTypes()[0], consumer));
        }
        if (added.isEmpty()) {
            throw new IllegalArgumentException(target + " has no @SubscribeEvent methods of the right kind (static: " + isStatic + ")");
        }
        registeredTargets.computeIfAbsent(target, t -> new CopyOnWriteArrayList<>()).addAll(added);
    }

    private static List<Method> collectMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method method : c.getDeclaredMethods()) {
                if (!method.isSynthetic() && !method.isBridge()) {
                    methods.add(method);
                }
            }
        }
        return methods;
    }

    @Override
    public void unregister(Object target) {
        List<Listener> removed = registeredTargets.remove(target);
        if (removed != null) {
            listeners.removeAll(removed);
            dispatchCache.clear();
        }
    }

    @Override
    public <T extends Event> void addListener(Consumer<T> listener) {
        addListener(EventPriority.NORMAL, false, listener);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, Consumer<T> listener) {
        addListener(priority, false, listener);
    }

    @Override
    public <T extends Event> void addListener(boolean receiveCanceled, Consumer<T> listener) {
        addListener(EventPriority.NORMAL, receiveCanceled, listener);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Consumer<T> listener) {
        addInternal(priority, receiveCanceled, resolveType(listener), listener);
    }

    @Override
    public <T extends Event> void addListener(Class<T> eventType, Consumer<T> listener) {
        addListener(EventPriority.NORMAL, false, eventType, listener);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, Class<T> eventType, Consumer<T> listener) {
        addListener(priority, false, eventType, listener);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Class<T> eventType, Consumer<T> listener) {
        addInternal(priority, receiveCanceled, eventType, listener);
    }

    @SuppressWarnings("unchecked")
    private Listener addInternal(EventPriority priority, boolean receiveCanceled, Class<?> type, Consumer<?> consumer) {
        Listener listener = new Listener(priority, receiveCanceled, type, (Consumer<Object>) consumer);
        listeners.add(listener);
        dispatchCache.clear();
        return listener;
    }

    private static Class<?> resolveType(Consumer<?> listener) {
        try {
            Class<?> type = TypeResolver.resolveRawArgument(Consumer.class, listener.getClass());
            if (type != null && type != TypeResolver.Unknown.class && Event.class.isAssignableFrom(type)) {
                return type;
            }
        } catch (Throwable ignored) {
            //Fall back to learning the type on dispatch
        }
        return null;
    }

    @Override
    public <T extends Event> T post(T event) {
        List<Listener> targets = dispatchCache.computeIfAbsent(event.getClass(), this::computeTargets);
        boolean cancellable = event instanceof ICancellableEvent;
        for (Listener listener : targets) {
            if (cancellable && !listener.receiveCanceled && ((ICancellableEvent) event).isCanceled()) {
                continue;
            }
            listener.dispatch(event);
        }
        return event;
    }

    private List<Listener> computeTargets(Class<?> eventClass) {
        List<Listener> result = new ArrayList<>();
        for (Listener listener : listeners) {
            if (listener.type == null || listener.type.isAssignableFrom(eventClass)) {
                result.add(listener);
            }
        }
        result.sort(Comparator.comparing(l -> l.priority));
        return result;
    }

    private static final class Listener {

        private final EventPriority priority;
        private final boolean receiveCanceled;
        private final Consumer<Object> consumer;
        private final Map<Class<?>, Boolean> untypedCompatibility;
        private final Class<?> type;

        private Listener(EventPriority priority, boolean receiveCanceled, Class<?> type, Consumer<Object> consumer) {
            this.priority = priority;
            this.receiveCanceled = receiveCanceled;
            this.type = type;
            this.consumer = consumer;
            this.untypedCompatibility = type == null ? new ConcurrentHashMap<>() : null;
        }

        private void dispatch(Object event) {
            if (untypedCompatibility == null) {
                consumer.accept(event);
                return;
            }
            Boolean compatible = untypedCompatibility.get(event.getClass());
            if (compatible == Boolean.FALSE) {
                return;
            }
            try {
                consumer.accept(event);
                if (compatible == null) {
                    untypedCompatibility.put(event.getClass(), Boolean.TRUE);
                }
            } catch (ClassCastException e) {
                StackTraceElement[] trace = e.getStackTrace();
                //Lambda proxies are hidden classes whose frames are omitted from stack traces, so a cast failure in the bridge method shows
                // up with this method as the top frame (or the proxy itself if hidden frames are shown)
                if (compatible == null && trace.length > 0 && (trace[0].getClassName().equals(consumer.getClass().getName())
                                                               || trace[0].getClassName().equals(Listener.class.getName()))) {
                    //Thrown by the lambda's bridge cast: this listener does not accept this event type
                    untypedCompatibility.put(event.getClass(), Boolean.FALSE);
                    return;
                }
                throw e;
            }
        }
    }
}
