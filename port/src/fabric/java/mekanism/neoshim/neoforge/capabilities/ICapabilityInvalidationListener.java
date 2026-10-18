package mekanism.neoshim.neoforge.capabilities;

@FunctionalInterface
public interface ICapabilityInvalidationListener {

    /** @return {@code true} to keep listening, {@code false} to be removed. */
    boolean onInvalidate();
}
