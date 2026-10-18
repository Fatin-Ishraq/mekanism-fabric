package mekanism.neoshim.fml;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import mekanism.neoshim.bus.EventBusImpl;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.neoforgespi.language.IModInfo;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge-like mod container. {@code modId} is the NeoForge-side id (e.g. {@code mekanism}); {@code fabricModId} is the id of the Fabric mod
 * that actually contains it (e.g. {@code mekanism_fabric}). See NOTES.md D9.
 */
public class ModContainer {

    private final String modId;
    private final String fabricModId;
    private final IModInfo modInfo;
    @Nullable
    private final IEventBus eventBus;
    private final Map<Class<?>, Supplier<?>> extensionPoints = new ConcurrentHashMap<>();

    public ModContainer(String modId, String fabricModId, @Nullable IEventBus eventBus) {
        this.modId = modId;
        this.fabricModId = fabricModId;
        this.eventBus = eventBus;
        this.modInfo = new IModInfo(modId, fabricModId);
    }

    /** Container for a plain Fabric mod (no NeoForge-style event bus). */
    public static ModContainer ofFabric(net.fabricmc.loader.api.ModContainer fabric) {
        String id = fabric.getMetadata().getId();
        return new ModContainer(id, id, null);
    }

    public String getModId() {
        return modId;
    }

    public String getNamespace() {
        return modId;
    }

    public String getFabricModId() {
        return fabricModId;
    }

    public IModInfo getModInfo() {
        return modInfo;
    }

    @Nullable
    public IEventBus getEventBus() {
        return eventBus;
    }

    public void registerConfig(ModConfig.Type type, IConfigSpec configSpec) {
        NeoForgeConfigRegistry.INSTANCE.register(modId, type, (ModConfigSpec) configSpec);
    }

    public void registerConfig(ModConfig.Type type, IConfigSpec configSpec, String fileName) {
        NeoForgeConfigRegistry.INSTANCE.register(modId, type, (ModConfigSpec) configSpec, fileName);
    }

    public <T extends IExtensionPoint> void registerExtensionPoint(Class<? extends T> point, Supplier<? extends T> extension) {
        extensionPoints.put(point, extension);
    }

    @SuppressWarnings("unchecked")
    public <T extends IExtensionPoint> Optional<T> getCustomExtension(Class<? extends T> point) {
        Supplier<?> supplier = extensionPoints.get(point);
        return supplier == null ? Optional.empty() : Optional.of((T) supplier.get());
    }

    public Optional<net.fabricmc.loader.api.ModContainer> getFabricContainer() {
        return FabricLoader.getInstance().getModContainer(fabricModId);
    }

    /** Creates the mod-bus of a NeoForge-style mod. */
    public static IEventBus createModBus(String modId) {
        return new EventBusImpl(modId + " mod bus");
    }
}
