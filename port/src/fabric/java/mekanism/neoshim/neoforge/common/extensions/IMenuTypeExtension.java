package mekanism.neoshim.neoforge.common.extensions;

import mekanism.neoshim.neoforge.network.IContainerFactory;
import mekanism.fabric.menu.ExtendedMenus;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface IMenuTypeExtension<T> {

    static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory) {
        return ExtendedMenus.create(factory);
    }
}
