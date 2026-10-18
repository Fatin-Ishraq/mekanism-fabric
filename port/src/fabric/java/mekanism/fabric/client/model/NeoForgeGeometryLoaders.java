package mekanism.fabric.client.model;

import mekanism.neoshim.neoforge.client.model.geometry.GeometryLoaderManager;
import mekanism.neoshim.neoforge.client.model.obj.ObjLoader;
import net.minecraft.resources.ResourceLocation;

/** Registers clean-room implementations of NeoForge's built-in model loaders. */
public final class NeoForgeGeometryLoaders {

    private NeoForgeGeometryLoaders() {
    }

    public static void register() {
        GeometryLoaderManager.register(id("obj"), ObjLoader.INSTANCE);
        GeometryLoaderManager.register(id("composite"), CompositeGeometry.LOADER);
        GeometryLoaderManager.register(id("fluid_container"), FluidContainerGeometry.LOADER);
        GeometryLoaderManager.register(id("item_layers"), ItemLayersGeometry.LOADER);
        GeometryLoaderManager.register(id("separate_transforms"), SeparateTransformsGeometry.LOADER);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("neoforge", path);
    }
}
