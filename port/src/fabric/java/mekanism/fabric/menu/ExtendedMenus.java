package mekanism.fabric.menu;

import io.netty.buffer.Unpooled;
import java.util.function.Consumer;
import mekanism.neoshim.neoforge.network.IContainerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge "extra data" menus on Fabric: the opening data written by the server-side consumer is captured as raw bytes and sent through an
 * {@link ExtendedScreenHandlerType}; the client re-wraps them in a {@link RegistryFriendlyByteBuf} for the {@link IContainerFactory}.
 */
public final class ExtendedMenus {

    private ExtendedMenus() {
    }

    public static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory) {
        return new ExtendedScreenHandlerType<>((syncId, inventory, data) -> {
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), inventory.player.registryAccess());
            return factory.create(syncId, inventory, buf);
        }, ByteBufCodecs.BYTE_ARRAY);
    }

    /** Implementation of NeoForge's {@code IPlayerExtension#openMenu(MenuProvider, Consumer<RegistryFriendlyByteBuf>)}. */
    public static java.util.OptionalInt openMenu(Player player, @Nullable MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        if (!(player instanceof ServerPlayer serverPlayer) || provider == null) {
            return java.util.OptionalInt.empty();
        }
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), serverPlayer.registryAccess());
        extraDataWriter.accept(buf);
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        buf.release();
        return serverPlayer.openMenu(new ExtendedScreenHandlerFactory<byte[]>() {
            @Override
            public byte[] getScreenOpeningData(ServerPlayer player) {
                return data;
            }

            @Override
            public Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                return provider.createMenu(containerId, playerInventory, player);
            }

            @Override
            public boolean shouldCloseCurrentScreen() {
                return provider.shouldCloseCurrentScreen();
            }
        });
    }
}
