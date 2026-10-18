package mekanism.neoshim.neoforge.network.handling;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public interface IPayloadContext {

    PacketListener listener();

    default Connection connection() {
        throw new UnsupportedOperationException("connection() is not available on Fabric");
    }

    Player player();

    void reply(CustomPacketPayload payload);

    void disconnect(Component reason);

    /** Fabric payload handlers already run on the main thread, so work runs immediately. */
    default CompletableFuture<Void> enqueueWork(Runnable task) {
        task.run();
        return CompletableFuture.completedFuture(null);
    }

    default <T> CompletableFuture<T> enqueueWork(Supplier<T> task) {
        return CompletableFuture.completedFuture(task.get());
    }

    PacketFlow flow();

    ConnectionProtocol protocol();

    default void finishCurrentTask(net.minecraft.server.network.ConfigurationTask.Type type) {
        throw new UnsupportedOperationException("finishCurrentTask is only available during configuration");
    }
}
