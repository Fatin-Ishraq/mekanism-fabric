package mekanism.fabric.menu;

import io.netty.buffer.Unpooled;
import java.util.OptionalInt;
import java.util.function.Consumer;
import mekanism.neoshim.neoforge.network.IContainerFactory;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge semantics for menus with extra opening data, on Fabric: any {@link MenuType} built from an {@link IContainerFactory} can receive
 * extra data. The server sends the bytes in a {@link MenuDataPayload} immediately before vanilla's open-screen packet (same connection, so
 * ordering is preserved); on the client, MenuTypeMixin hands the pending bytes to the factory when the menu is created.
 */
public final class ExtendedMenus {

    @Nullable
    private static byte[] pendingClientData;

    private ExtendedMenus() {
    }

    public record MenuDataPayload(byte[] data) implements CustomPacketPayload {

        public static final Type<MenuDataPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("mekanism_fabric", "menu_data"));
        public static final StreamCodec<RegistryFriendlyByteBuf, MenuDataPayload> STREAM_CODEC = ByteBufCodecs.BYTE_ARRAY.<RegistryFriendlyByteBuf>cast()
              .map(MenuDataPayload::new, MenuDataPayload::data);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void init() {
        PayloadTypeRegistry.playS2C().register(MenuDataPayload.TYPE, MenuDataPayload.STREAM_CODEC);
    }

    /** Client: called by the payload receiver. */
    public static void setPendingClientData(byte[] data) {
        pendingClientData = data;
    }

    /** Client: consumes the pending extra data for the menu being created (null if the menu was opened without data). */
    @Nullable
    public static byte[] takePendingClientData() {
        byte[] data = pendingClientData;
        pendingClientData = null;
        return data;
    }

    public static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory) {
        return new MenuType<>(factory, FeatureFlags.VANILLA_SET);
    }

    /** Implementation of NeoForge's {@code IPlayerExtension#openMenu(MenuProvider, Consumer<RegistryFriendlyByteBuf>)}. */
    public static OptionalInt openMenu(Player player, @Nullable MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        if (!(player instanceof ServerPlayer serverPlayer) || provider == null) {
            return OptionalInt.empty();
        }
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), serverPlayer.registryAccess());
        byte[] data;
        try {
            extraDataWriter.accept(buf);
            data = new byte[buf.readableBytes()];
            buf.readBytes(data);
        } finally {
            buf.release();
        }
        ServerPlayNetworking.send(serverPlayer, new MenuDataPayload(data));
        return serverPlayer.openMenu(provider);
    }
}
