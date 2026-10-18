package mekanism.neoshim.neoforge.common.util;

import io.netty.buffer.Unpooled;
import java.util.function.Consumer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class FriendlyByteBufUtil {

    public static byte[] writeCustomData(Consumer<RegistryFriendlyByteBuf> dataWriter, RegistryAccess registryAccess) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
        try {
            dataWriter.accept(buf);
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            return data;
        } finally {
            buf.release();
        }
    }
}
