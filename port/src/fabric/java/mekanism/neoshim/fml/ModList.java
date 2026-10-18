package mekanism.neoshim.fml;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.fabric.scan.ScanDataLoader;
import mekanism.neoshim.neoforgespi.language.ModFileScanData;
import net.fabricmc.loader.api.FabricLoader;

public class ModList {

    private static final ModList INSTANCE = new ModList();
    private final Map<String, ModContainer> neoContainers = new ConcurrentHashMap<>();

    public static ModList get() {
        return INSTANCE;
    }

    /** Registers a NeoForge-style container (e.g. "mekanism" provided by "mekanism_fabric"). */
    public void registerContainer(ModContainer container) {
        neoContainers.put(container.getModId(), container);
    }

    public boolean isLoaded(String modId) {
        return neoContainers.containsKey(modId) || FabricLoader.getInstance().isModLoaded(modId);
    }

    public Optional<? extends ModContainer> getModContainerById(String modId) {
        ModContainer container = neoContainers.get(modId);
        if (container != null) {
            return Optional.of(container);
        }
        return FabricLoader.getInstance().getModContainer(modId).map(ModContainer::ofFabric);
    }

    public int size() {
        return FabricLoader.getInstance().getAllMods().size();
    }

    public List<ModFileScanData> getAllScanData() {
        return ScanDataLoader.getAllScanData();
    }
}
