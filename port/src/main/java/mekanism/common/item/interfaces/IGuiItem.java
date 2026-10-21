package mekanism.common.item.interfaces;

import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import mekanism.neoshim.neoforge.transfer.item.ItemResource;

public interface IGuiItem {

    ContainerTypeRegistryObject<?> getContainerType();

    default void encodeContainerData(RegistryFriendlyByteBuf buf, ItemResource itemType) {
    }
}
