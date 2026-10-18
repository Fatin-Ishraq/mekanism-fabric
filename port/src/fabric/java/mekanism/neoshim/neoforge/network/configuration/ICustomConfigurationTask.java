package mekanism.neoshim.neoforge.network.configuration;

import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;

public interface ICustomConfigurationTask extends ConfigurationTask {

    void run(Consumer<CustomPacketPayload> sender);

    @Override
    default void start(Consumer<Packet<?>> sender) {
        run(payload -> sender.accept(new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(payload)));
    }
}
