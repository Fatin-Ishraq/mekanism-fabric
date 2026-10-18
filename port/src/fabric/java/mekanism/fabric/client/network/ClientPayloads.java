package mekanism.fabric.client.network;

import mekanism.neoshim.neoforge.network.handling.IPayloadContext;
import mekanism.neoshim.neoforge.network.handling.IPayloadHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

/** Client half of {@link mekanism.fabric.network.FabricPayloads}; only loaded on the physical client. */
public final class ClientPayloads {

    private ClientPayloads() {
    }

    public static <T extends CustomPacketPayload> void registerPlay(CustomPacketPayload.Type<T> type, IPayloadHandler<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.handle(payload, new IPayloadContext() {
            @Override
            public PacketListener listener() {
                return context.client().getConnection();
            }

            @Override
            public Player player() {
                return context.player();
            }

            @Override
            public void reply(CustomPacketPayload reply) {
                context.responseSender().sendPacket(reply);
            }

            @Override
            public void disconnect(Component reason) {
                if (context.client().getConnection() != null) {
                    context.client().getConnection().getConnection().disconnect(reason);
                }
            }

            @Override
            public PacketFlow flow() {
                return PacketFlow.CLIENTBOUND;
            }

            @Override
            public ConnectionProtocol protocol() {
                return ConnectionProtocol.PLAY;
            }
        }));
    }

    public static <T extends CustomPacketPayload> void registerConfiguration(CustomPacketPayload.Type<T> type, IPayloadHandler<T> handler) {
        ClientConfigurationNetworking.registerGlobalReceiver(type, (payload, context) -> handler.handle(payload, new IPayloadContext() {
            @Override
            public PacketListener listener() {
                return null;
            }

            @Override
            public Player player() {
                return Minecraft.getInstance().player;
            }

            @Override
            public void reply(CustomPacketPayload reply) {
                context.responseSender().sendPacket(reply);
            }

            @Override
            public void disconnect(Component reason) {
            }

            @Override
            public PacketFlow flow() {
                return PacketFlow.CLIENTBOUND;
            }

            @Override
            public ConnectionProtocol protocol() {
                return ConnectionProtocol.CONFIGURATION;
            }
        }));
    }

    public static void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
