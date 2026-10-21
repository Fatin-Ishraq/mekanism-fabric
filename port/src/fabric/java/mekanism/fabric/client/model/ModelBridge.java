package mekanism.fabric.client.model;

import java.util.LinkedHashMap;
import java.util.Map;
import mekanism.fabric.bridge.ModBusLifecycle;
import mekanism.neoshim.fml.ModContainer;
import mekanism.neoshim.neoforge.client.event.ModelEvent;
import mekanism.neoshim.neoforge.client.event.RegisterBlockStateModels;
import mekanism.neoshim.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import mekanism.neoshim.neoforge.client.event.RegisterItemModelsEvent;
import mekanism.neoshim.neoforge.client.event.RegisterSelectItemModelPropertyEvent;
import mekanism.neoshim.neoforge.client.event.RegisterSpecialModelRendererEvent;
import mekanism.neoshim.neoforge.client.model.UnbakedModelLoader;
import mekanism.neoshim.neoforge.client.model.standalone.StandaloneModelKey;
import mekanism.neoshim.neoforge.client.model.standalone.UnbakedStandaloneModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedExtraModel;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.resources.Identifier;

/**
 * Connects NeoForge's model registration events to Fabric's model loading API. Model JSON {@code loader} fields are rewritten to
 * {@code fabric:type} when resources are processed (build.gradle), so loaders registered here are found by Fabric's model deserializer.
 */
public final class ModelBridge {

    private ModelBridge() {
    }

    public static void init() {
        Map<Identifier, UnbakedModelLoader<?>> loaders = new LinkedHashMap<>();
        loaders.put(CompositeModel.ID, CompositeModel.LOADER);
        for (ModContainer mod : ModBusLifecycle.mods()) {
            mod.getEventBus().post(new ModelEvent.RegisterLoaders(loaders));
            mod.getEventBus().post(new RegisterBlockStateModels());
            mod.getEventBus().post(new RegisterItemModelsEvent());
            mod.getEventBus().post(new RegisterSpecialModelRendererEvent());
            mod.getEventBus().post(new RegisterSelectItemModelPropertyEvent());
            mod.getEventBus().post(new RegisterConditionalItemModelPropertyEvent());
        }
        loaders.forEach((id, loader) -> UnbakedModelDeserializer.register(id, loader::read));

        ModelLoadingPlugin.register(context -> {
            Map<StandaloneModelKey<?>, UnbakedStandaloneModel<?>> standalone = new LinkedHashMap<>();
            for (ModContainer mod : ModBusLifecycle.mods()) {
                mod.getEventBus().post(new ModelEvent.RegisterStandalone(standalone));
            }
            standalone.forEach((key, model) -> addStandalone(context, key, model));
        });
    }

    private static <T> void addStandalone(ModelLoadingPlugin.Context context, StandaloneModelKey<?> rawKey, UnbakedStandaloneModel<?> rawModel) {
        @SuppressWarnings("unchecked")
        StandaloneModelKey<T> key = (StandaloneModelKey<T>) rawKey;
        @SuppressWarnings("unchecked")
        UnbakedStandaloneModel<T> model = (UnbakedStandaloneModel<T>) rawModel;
        ExtraModelKey<T> fabricKey = key.fabricKey();
        context.addModel(fabricKey, new UnbakedExtraModel<>() {
            @Override
            public T bake(ModelBaker baker) {
                return model.bake(baker, key::getName);
            }

            @Override
            public void resolveDependencies(Resolver resolver) {
                model.resolveDependencies(resolver);
            }
        });
    }
}
