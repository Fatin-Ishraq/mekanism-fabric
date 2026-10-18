package mekanism.neoshim.neoforge.capabilities;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ICapabilityProvider<O, C, T> {

    @Nullable
    T getCapability(O object, C context);
}
