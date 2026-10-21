package mekanism.neoshim.neoforge.client.model.standalone;

import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.Identifier;

/** A standalone model baked from a single model file. */
public final class SimpleUnbakedStandaloneModel<T> implements UnbakedStandaloneModel<T> {

    private final Identifier modelId;
    private final SimpleBaker<T> baker;

    public SimpleUnbakedStandaloneModel(Identifier modelId, SimpleBaker<T> baker) {
        this.modelId = modelId;
        this.baker = baker;
    }

    @Override
    public T bake(ModelBaker modelBaker, ModelDebugName debugName) {
        return baker.bake(modelBaker.getModel(modelId), modelBaker);
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        resolver.markDependency(modelId);
    }

    public static SimpleUnbakedStandaloneModel<BlockStateModelPart> simpleModelWrapper(Identifier modelId) {
        return simpleModelWrapper(modelId, BlockModelRotation.IDENTITY);
    }

    public static SimpleUnbakedStandaloneModel<BlockStateModelPart> simpleModelWrapper(Identifier modelId, ModelState state) {
        return new SimpleUnbakedStandaloneModel<>(modelId, (model, baker) -> SimpleModelWrapper.bake(baker, modelId, state));
    }

    public static SimpleUnbakedStandaloneModel<BlockStateModel> blockStateModel(Identifier modelId) {
        return blockStateModel(modelId, BlockModelRotation.IDENTITY);
    }

    public static SimpleUnbakedStandaloneModel<BlockStateModel> blockStateModel(Identifier modelId, ModelState state) {
        return new SimpleUnbakedStandaloneModel<>(modelId, (model, baker) -> new SingleVariant(SimpleModelWrapper.bake(baker, modelId, state)));
    }

    public static SimpleUnbakedStandaloneModel<QuadCollection> quadCollection(Identifier modelId) {
        return quadCollection(modelId, BlockModelRotation.IDENTITY);
    }

    public static SimpleUnbakedStandaloneModel<QuadCollection> quadCollection(Identifier modelId, ModelState state) {
        return new SimpleUnbakedStandaloneModel<>(modelId, (model, baker) -> model.bakeTopGeometry(model.getTopTextureSlots(), baker, state));
    }

    @FunctionalInterface
    public interface SimpleBaker<T> {

        T bake(ResolvedModel model, ModelBaker baker);
    }
}
