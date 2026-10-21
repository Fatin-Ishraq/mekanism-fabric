package mekanism.common.component.containers.creator;

import mekanism.neoshim.neoforge.transfer.access.ItemAccess;

@FunctionalInterface
public interface IBasicContainerCreator<CONTAINER> {

    CONTAINER create(ItemAccess attachedAccess, int containerIndex);
}