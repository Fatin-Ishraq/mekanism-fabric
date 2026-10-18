package mekanism.neoshim.fml;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

public class InterModComms {

    private static final Map<String, Queue<IMCMessage>> CONTAINER_QUEUES = new ConcurrentHashMap<>();
    private static final ThreadLocal<String> CURRENT_SENDER = new ThreadLocal<>();

    public record IMCMessage(String senderModId, String modId, String method, Supplier<?> messageSupplier) {

        @SuppressWarnings("unchecked")
        public <T> Supplier<T> getMessageSupplier() {
            return (Supplier<T>) messageSupplier;
        }
    }

    /** Set by the bootstrap while firing {@code InterModEnqueueEvent} for a given mod. */
    public static void setCurrentSender(@Nullable String modId) {
        if (modId == null) {
            CURRENT_SENDER.remove();
        } else {
            CURRENT_SENDER.set(modId);
        }
    }

    public static boolean sendTo(String modId, String method, Supplier<?> thing) {
        String sender = CURRENT_SENDER.get();
        return sendTo(sender == null ? "unknown" : sender, modId, method, thing);
    }

    public static boolean sendTo(String senderModId, String modId, String method, Supplier<?> thing) {
        CONTAINER_QUEUES.computeIfAbsent(modId, k -> new ConcurrentLinkedQueue<>()).add(new IMCMessage(senderModId, modId, method, thing));
        return true;
    }

    public static Stream<IMCMessage> getMessages(String modId, Predicate<String> methodMatcher) {
        Queue<IMCMessage> queue = CONTAINER_QUEUES.get(modId);
        if (queue == null) {
            return Stream.empty();
        }
        Stream.Builder<IMCMessage> builder = Stream.builder();
        queue.removeIf(message -> {
            if (methodMatcher.test(message.method())) {
                builder.add(message);
                return true;
            }
            return false;
        });
        return builder.build();
    }

    public static Stream<IMCMessage> getMessages(String modId) {
        return getMessages(modId, s -> true);
    }
}
