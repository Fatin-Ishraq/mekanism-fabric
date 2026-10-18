package mekanism.fabric.network;

import mekanism.neoshim.fml.loading.FMLEnvironment;
import mekanism.neoshim.neoforge.network.handling.IPayloadContext;
import mekanism.neoshim.neoforge.network.handling.IPayloadHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/** Fabric Networking API v1 backend for the shim's PayloadRegistrar / PacketDistributor (NOTES.md D6). */
public final class FabricPayloads {

    private FabricPayloads() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> void playToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
          @Nullable IPayloadHandler<T> handler) {
        PayloadTypeRegistry.playS2C().register(type, (StreamCodec<RegistryFriendlyByteBuf, T>) codec);
        if (handler != null && FMLEnvironment.dist.isClient()) {
            mekanism.fabric.client.network.ClientPayloads.registerPlay(type, handler);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> void playToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
          IPayloadHandler<T> handler) {
        PayloadTypeRegistry.playC2S().register(type, (StreamCodec<RegistryFriendlyByteBuf, T>) codec);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.handle(payload, new IPayloadContext() {
            @Override
            public PacketListener listener() {
                return context.player().connection;
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
                context.player().connection.disconnect(reason);
            }

            @Override
            public PacketFlow flow() {
                return PacketFlow.SERVERBOUND;
            }

            @Override
            public ConnectionProtocol protocol() {
                return ConnectionProtocol.PLAY;
            }
        }));
    }

    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> void configurationToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec,
          IPayloadHandler<T> handler) {
        PayloadTypeRegistry.configurationS2C().register(type, (StreamCodec<FriendlyByteBuf, T>) codec);
        if (FMLEnvironment.dist.isClient()) {
            mekanism.fabric.client.network.ClientPayloads.registerConfiguration(type, handler);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> void configurationToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec,
          IPayloadHandler<T> handler) {
        PayloadTypeRegistry.configurationC2S().register(type, (StreamCodec<FriendlyByteBuf, T>) codec);
        ServerConfigurationNetworking.registerGlobalReceiver(type, (payload, context) -> handler.handle(payload, new IPayloadContext() {
            @Override
            public PacketListener listener() {
                return context.networkHandler();
            }

            @Override
            public Player player() {
                throw new UnsupportedOperationException("No player during configuration");
            }

            @Override
            public void reply(CustomPacketPayload reply) {
                context.responseSender().sendPacket(reply);
            }

            @Override
            public void disconnect(Component reason) {
                context.networkHandler().disconnect(reason);
            }

            @Override
            public PacketFlow flow() {
                return PacketFlow.SERVERBOUND;
            }

            @Override
            public ConnectionProtocol protocol() {
                return ConnectionProtocol.CONFIGURATION;
            }
        }));
    }

    public static void sendToServer(CustomPacketPayload payload) {
        mekanism.fabric.client.network.ClientPayloads.sendToServer(payload);
    }
}
