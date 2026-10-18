package mekanism.fabric;

import java.util.ArrayList;
import java.util.List;
import mekanism.fabric.bridge.ModBusLifecycle;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.fml.ModContainer;
import mekanism.neoshim.fml.ModList;
import net.fabricmc.loader.api.FabricLoader;

/** Discovers and constructs {@link NeoModule}s. */
public final class ModuleLoader {

    private record Loaded(NeoModule module, ModContainer container) {
    }

    private static final List<Loaded> LOADED = new ArrayList<>();

    private ModuleLoader() {
    }

    public static void constructModules() {
        for (NeoModule module : FabricLoader.getInstance().getEntrypoints("mekanism_fabric:module", NeoModule.class)) {
            IEventBus modBus = ModContainer.createModBus(module.neoModId());
            ModContainer container = new ModContainer(module.neoModId(), module.fabricModId(), modBus);
            ModList.get().registerContainer(container);
            ModBusLifecycle.addMod(container);
            module.construct(container, modBus);
            EventBusSubscribers.register(modBus, module.commonSubscribers());
            LOADED.add(new Loaded(module, container));
        }
    }

    public static void constructClientModules() {
        for (Loaded loaded : LOADED) {
            loaded.module().constructClient(loaded.container());
            EventBusSubscribers.register(loaded.container().getEventBus(), loaded.module().clientSubscribers());
        }
    }
}
