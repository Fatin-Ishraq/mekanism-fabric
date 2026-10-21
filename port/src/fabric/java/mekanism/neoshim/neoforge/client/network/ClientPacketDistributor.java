package mekanism.neoshim.neoforge.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client to server payload sending. */
public final class ClientPacketDistributor {

    private ClientPacketDistributor() {
    }

    public static void sendToServer(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        ClientPlayNetworking.send(payload);
        for (CustomPacketPayload other : payloads) {
            ClientPlayNetworking.send(other);
        }
    }
}
