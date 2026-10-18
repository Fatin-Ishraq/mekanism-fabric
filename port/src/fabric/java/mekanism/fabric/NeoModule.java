package mekanism.fabric;

import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.fml.ModContainer;

/**
 * Entrypoint ({@code "mekanism_fabric:module"} in fabric.mod.json) for additional NeoForge-style Mekanism modules (Generators, Additions,
 * Tools). Core constructs them right after Mekanism itself so they take part in the same NeoForge-ordered mod-bus lifecycle.
 */
public interface NeoModule {

    /** NeoForge mod id / resource namespace, e.g. "mekanismgenerators". */
    String neoModId();

    /** Fabric mod id of the jar providing it, e.g. "mekanismgenerators_fabric". */
    String fabricModId();

    /** Constructs the module's @Mod class. */
    void construct(ModContainer container, IEventBus modBus);

    /** Classes annotated with @EventBusSubscriber (common). */
    default String[] commonSubscribers() {
        return new String[0];
    }

    /** Client-only: constructs the module's client @Mod class. */
    default void constructClient(ModContainer container) {
    }

    /** Client-only @EventBusSubscriber classes. */
    default String[] clientSubscribers() {
        return new String[0];
    }
}
