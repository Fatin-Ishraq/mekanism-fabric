package mekanism.neoshim.neoforge.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public interface IContainerFactory<T extends AbstractContainerMenu> extends MenuType.MenuSupplier<T> {

    T create(int windowId, Inventory inv, @Nullable RegistryFriendlyByteBuf data);

    @Override
    default T create(int windowId, Inventory inv) {
        return create(windowId, inv, null);
    }
}
