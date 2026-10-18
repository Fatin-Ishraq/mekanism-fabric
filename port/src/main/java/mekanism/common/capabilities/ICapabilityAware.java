package mekanism.common.capabilities;

import mekanism.neoshim.neoforge.capabilities.RegisterCapabilitiesEvent;

@FunctionalInterface
public interface ICapabilityAware {

    void attachCapabilities(RegisterCapabilitiesEvent event);
}