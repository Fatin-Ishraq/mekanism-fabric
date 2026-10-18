package mekanism.fabric.entity;

import io.netty.buffer.Unpooled;
import mekanism.neoshim.neoforge.entity.IEntityWithComplexSpawn;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * NeoForge's {@link IEntityWithComplexSpawn}: ServerEntityMixin appends a {@link SpawnDataPayload} right after the add-entity packet in the pairing bundle (as
 * NeoForge does), and the client receiver hands the bytes to the freshly added entity.
 */
public final class ComplexSpawns {

    private ComplexSpawns() {
    }

    public record SpawnDataPayload(int entityId, byte[] data) implements CustomPacketPayload {

        public static final Type<SpawnDataPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("mekanism_fabric", "spawn_data"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SpawnDataPayload> STREAM_CODEC = StreamCodec.composite(
              ByteBufCodecs.VAR_INT, SpawnDataPayload::entityId,
              ByteBufCodecs.BYTE_ARRAY, SpawnDataPayload::data,
              SpawnDataPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void init() {
        PayloadTypeRegistry.playS2C().register(SpawnDataPayload.TYPE, SpawnDataPayload.STREAM_CODEC);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Packet<ClientGamePacketListener> createPacket(Entity entity, IEntityWithComplexSpawn complexSpawn) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), entity.registryAccess());
        try {
            complexSpawn.writeSpawnData(buf);
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            //The custom payload packet is a common packet; the client game listener handles it inside bundles as well
            return (Packet) ServerPlayNetworking.createS2CPacket(new SpawnDataPayload(entity.getId(), data));
        } finally {
            buf.release();
        }
    }

    /** Client: applies received spawn data. */
    public static void apply(Level level, RegistryAccess registryAccess, SpawnDataPayload payload) {
        if (level.getEntity(payload.entityId()) instanceof IEntityWithComplexSpawn complexSpawn) {
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(payload.data()), registryAccess);
            try {
                complexSpawn.readSpawnData(buf);
            } finally {
                buf.release();
            }
        }
    }
}
