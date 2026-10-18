package mekanism.neoshim.neoforge.common.extensions;

import java.util.OptionalInt;
import java.util.function.Consumer;
import mekanism.fabric.menu.ExtendedMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link Player}. */
public interface IPlayerExtension {

    default OptionalInt openMenu(@Nullable MenuProvider menuProvider, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        return ExtendedMenus.openMenu((Player) this, menuProvider, extraDataWriter);
    }
}
