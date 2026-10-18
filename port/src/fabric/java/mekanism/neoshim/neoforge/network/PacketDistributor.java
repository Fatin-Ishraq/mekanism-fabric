package mekanism.neoshim.neoforge.network;

import mekanism.fabric.network.FabricPayloads;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

public final class PacketDistributor {

    private PacketDistributor() {
    }

    public static void sendToServer(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        FabricPayloads.sendToServer(payload);
        for (CustomPacketPayload p : payloads) {
            FabricPayloads.sendToServer(p);
        }
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        ServerPlayNetworking.send(player, payload);
        for (CustomPacketPayload p : payloads) {
            ServerPlayNetworking.send(player, p);
        }
    }

    public static void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        for (ServerPlayer player : PlayerLookup.world(level)) {
            sendToPlayer(player, payload, payloads);
        }
    }

    public static void sendToPlayersNear(ServerLevel level, @Nullable ServerPlayer excluded, double x, double y, double z, double radius,
          CustomPacketPayload payload, CustomPacketPayload... payloads) {
        for (ServerPlayer player : PlayerLookup.around(level, new net.minecraft.world.phys.Vec3(x, y, z), radius)) {
            if (player != excluded) {
                sendToPlayer(player, payload, payloads);
            }
        }
    }

    public static void sendToAllPlayers(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        net.minecraft.server.MinecraftServer server = mekanism.neoshim.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            for (ServerPlayer player : PlayerLookup.all(server)) {
                sendToPlayer(player, payload, payloads);
            }
        }
    }

    public static void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            sendToPlayer(player, payload, payloads);
        }
    }

    public static void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        sendToPlayersTrackingEntity(entity, payload, payloads);
        if (entity instanceof ServerPlayer player) {
            sendToPlayer(player, payload, payloads);
        }
    }

    public static void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunkPos, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        for (ServerPlayer player : PlayerLookup.tracking(level, chunkPos)) {
            sendToPlayer(player, payload, payloads);
        }
    }
}
