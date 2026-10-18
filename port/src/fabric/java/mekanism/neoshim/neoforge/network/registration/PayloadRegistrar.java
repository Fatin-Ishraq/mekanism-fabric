package mekanism.neoshim.neoforge.network.registration;

import mekanism.fabric.network.FabricPayloads;
import mekanism.neoshim.neoforge.network.handling.HandlerThread;
import mekanism.neoshim.neoforge.network.handling.IPayloadHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Registers payload types and handlers with Fabric Networking API v1. The version string is not negotiated on Fabric. */
public class PayloadRegistrar {

    private final String version;
    private boolean optional = false;
    private HandlerThread thread = HandlerThread.MAIN;

    public PayloadRegistrar(String version) {
        this.version = version;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar playToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> reader,
          IPayloadHandler<T> handler) {
        FabricPayloads.playToClient(type, reader, handler);
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar playToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> reader) {
        FabricPayloads.playToClient(type, reader, null);
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar playToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> reader,
          IPayloadHandler<T> handler) {
        FabricPayloads.playToServer(type, reader, handler);
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar playBidirectional(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> reader,
          IPayloadHandler<T> handler) {
        FabricPayloads.playToClient(type, reader, handler);
        FabricPayloads.playToServer(type, reader, handler);
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar configurationToClient(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> reader,
          IPayloadHandler<T> handler) {
        FabricPayloads.configurationToClient(type, reader, handler);
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar configurationToServer(CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> reader,
          IPayloadHandler<T> handler) {
        FabricPayloads.configurationToServer(type, reader, handler);
        return this;
    }

    public PayloadRegistrar versioned(String version) {
        return new PayloadRegistrar(version);
    }

    public PayloadRegistrar optional() {
        PayloadRegistrar registrar = new PayloadRegistrar(version);
        registrar.optional = true;
        registrar.thread = thread;
        return registrar;
    }

    public PayloadRegistrar executesOn(HandlerThread thread) {
        PayloadRegistrar registrar = new PayloadRegistrar(version);
        registrar.optional = optional;
        registrar.thread = thread;
        return registrar;
    }

    public String getVersion() {
        return version;
    }
}
