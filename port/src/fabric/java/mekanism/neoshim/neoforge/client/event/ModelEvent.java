package mekanism.neoshim.neoforge.client.event;

import java.util.Map;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import mekanism.neoshim.neoforge.client.model.UnbakedModelLoader;
import mekanism.neoshim.neoforge.client.model.standalone.StandaloneModelKey;
import mekanism.neoshim.neoforge.client.model.standalone.UnbakedStandaloneModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public abstract class ModelEvent extends Event {

    protected ModelEvent() {
    }

    /** Fired after the model manager applied a reload. The 26.1 model manager keeps no bakery, so {@link #getModelBakery()} is null. */
    public static class BakingCompleted extends ModelEvent implements IModBusEvent {

        private final ModelManager modelManager;
        private final ModelBakery.BakingResult bakingResult;
        @Nullable
        private final ModelBakery modelBakery;

        public BakingCompleted(ModelManager modelManager, ModelBakery.BakingResult bakingResult, @Nullable ModelBakery modelBakery) {
            this.modelManager = modelManager;
            this.bakingResult = bakingResult;
            this.modelBakery = modelBakery;
        }

        public ModelManager getModelManager() {
            return modelManager;
        }

        public ModelBakery.BakingResult getBakingResult() {
            return bakingResult;
        }

        @Nullable
        public ModelBakery getModelBakery() {
            return modelBakery;
        }
    }

    /** Custom model loaders, selected by the {@code loader} field of a model JSON. */
    public static class RegisterLoaders extends ModelEvent implements IModBusEvent {

        private final Map<Identifier, UnbakedModelLoader<?>> loaders;

        public RegisterLoaders(Map<Identifier, UnbakedModelLoader<?>> loaders) {
            this.loaders = loaders;
        }

        public void register(Identifier key, UnbakedModelLoader<?> loader) {
            if (loaders.putIfAbsent(key, loader) != null) {
                throw new IllegalStateException("Model loader already registered: " + key);
            }
        }
    }

    /** Models baked on their own (outside block states and items), read back via the baking result's standalone models. */
    public static class RegisterStandalone extends ModelEvent implements IModBusEvent {

        private final Map<StandaloneModelKey<?>, UnbakedStandaloneModel<?>> models;

        public RegisterStandalone(Map<StandaloneModelKey<?>, UnbakedStandaloneModel<?>> models) {
            this.models = models;
        }

        public <T> void register(StandaloneModelKey<T> key, UnbakedStandaloneModel<T> model) {
            if (models.putIfAbsent(key, model) != null) {
                throw new IllegalStateException("Duplicate standalone model registration: " + key);
            }
        }
    }
}
