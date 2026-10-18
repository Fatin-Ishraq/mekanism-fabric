package mekanism.neoshim.neoforge.client.model.geometry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/** Registry of custom geometry loaders, consulted by the port's Fabric model loading plugin for {@code "loader"} keys in block models. */
public final class GeometryLoaderManager {

    private static final Map<ResourceLocation, IGeometryLoader<?>> LOADERS = new ConcurrentHashMap<>();

    private GeometryLoaderManager() {
    }

    public static void register(ResourceLocation id, IGeometryLoader<?> loader) {
        LOADERS.put(id, loader);
    }

    @Nullable
    public static IGeometryLoader<?> get(ResourceLocation id) {
        return LOADERS.get(id);
    }

    public static Map<ResourceLocation, IGeometryLoader<?>> all() {
        return LOADERS;
    }
}
