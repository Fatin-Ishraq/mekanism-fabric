package mekanism.neoshim.neoforge.client.event;

import java.util.Map;
import java.util.Set;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import mekanism.neoshim.neoforge.client.model.geometry.IGeometryLoader;
import mekanism.neoshim.neoforge.client.model.geometry.GeometryLoaderManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public abstract class ModelEvent extends Event {

    public static class ModifyBakingResult extends ModelEvent implements IModBusEvent {

        private final Map<ModelResourceLocation, BakedModel> models;
        private final ModelBakery modelBakery;

        public ModifyBakingResult(Map<ModelResourceLocation, BakedModel> models, ModelBakery modelBakery) {
            this.models = models;
            this.modelBakery = modelBakery;
        }

        public Map<ModelResourceLocation, BakedModel> getModels() {
            return models;
        }

        public ModelBakery getModelBakery() {
            return modelBakery;
        }
    }

    public static class BakingCompleted extends ModelEvent implements IModBusEvent {

        private final ModelManager modelManager;
        private final Map<ModelResourceLocation, BakedModel> models;
        private final ModelBakery modelBakery;

        public BakingCompleted(ModelManager modelManager, Map<ModelResourceLocation, BakedModel> models, ModelBakery modelBakery) {
            this.modelManager = modelManager;
            this.models = models;
            this.modelBakery = modelBakery;
        }

        public ModelManager getModelManager() {
            return modelManager;
        }

        public Map<ModelResourceLocation, BakedModel> getModels() {
            return models;
        }

        public ModelBakery getModelBakery() {
            return modelBakery;
        }
    }

    public static class RegisterAdditional extends ModelEvent implements IModBusEvent {

        private final Set<ModelResourceLocation> models;

        public RegisterAdditional(Set<ModelResourceLocation> models) {
            this.models = models;
        }

        public void register(ModelResourceLocation model) {
            models.add(model);
        }
    }

    public static class RegisterGeometryLoaders extends ModelEvent implements IModBusEvent {

        public void register(ResourceLocation key, IGeometryLoader<?> loader) {
            GeometryLoaderManager.register(key, loader);
        }
    }
}
