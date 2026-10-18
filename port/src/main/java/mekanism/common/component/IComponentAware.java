package mekanism.common.component;

import mekanism.neoshim.bus.api.IEventBus;

@FunctionalInterface
public interface IComponentAware {

    void addComponents(IEventBus eventBus);
}