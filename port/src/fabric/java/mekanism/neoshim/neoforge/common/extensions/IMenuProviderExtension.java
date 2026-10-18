package mekanism.neoshim.neoforge.common.extensions;

/** Injected into {@link net.minecraft.world.MenuProvider}. */
public interface IMenuProviderExtension {

    default boolean shouldTriggerClientSideContainerClosingOnOpen() {
        return true;
    }
}
