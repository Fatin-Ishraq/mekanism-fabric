package mekanism.fabric.client.model;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import mekanism.neoshim.neoforge.client.model.geometry.IUnbakedGeometry;
import net.minecraft.client.renderer.block.model.BlockModel;
import org.jetbrains.annotations.Nullable;

/** Replacement for NeoForge's {@code BlockModel#customData.getCustomGeometry()}; populated by the port's model loading plugin. */
public final class CustomGeometryAccess {

    private static final Map<BlockModel, IUnbakedGeometry<?>> GEOMETRY = Collections.synchronizedMap(new WeakHashMap<>());

    private CustomGeometryAccess() {
    }

    public static void setCustomGeometry(BlockModel model, IUnbakedGeometry<?> geometry) {
        GEOMETRY.put(model, geometry);
    }

    @Nullable
    public static IUnbakedGeometry<?> getCustomGeometry(BlockModel model) {
        return GEOMETRY.get(model);
    }
}
