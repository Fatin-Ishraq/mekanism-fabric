package mekanism.fabric.client.model;

import java.util.List;
import java.util.function.Supplier;
import mekanism.neoshim.neoforge.client.ChunkRenderTypeSet;
import mekanism.neoshim.neoforge.client.model.data.ModelData;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Renders NeoForge-style baked models (ModelData + per-render-type quads) through the Fabric Renderer API: model data comes from the block
 * entity ({@code getRenderData()}, see BlockEntityRenderDataMixin), quads of each render type are emitted with the matching blend mode.
 */
public final class NeoModelRendering {

    private static final Direction[] CULL_FACES = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null};

    private NeoModelRendering() {
    }

    @Nullable
    private static RenderMaterial material(RenderType type) {
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        if (renderer == null) {
            return null;
        }
        return renderer.materialFinder().blendMode(BlendMode.fromRenderLayer(type)).find();
    }

    public static void emitBlockQuads(BakedModel model, BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier,
          RenderContext context) {
        ModelData data = blockView.getBlockEntityRenderData(pos) instanceof ModelData modelData ? modelData : ModelData.EMPTY;
        data = model.getModelData(blockView, pos, state, data);
        ChunkRenderTypeSet types = model.getRenderTypes(state, randomSupplier.get(), data);
        QuadEmitter emitter = context.getEmitter();
        for (RenderType type : types) {
            RenderMaterial material = material(type);
            for (Direction side : CULL_FACES) {
                RandomSource random = randomSupplier.get();
                List<BakedQuad> quads = model.getQuads(state, side, random, data, type);
                for (BakedQuad quad : quads) {
                    emitter.fromVanilla(quad, material, side);
                    emitter.emit();
                }
            }
        }
    }

    public static void emitItemQuads(BakedModel model, ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        QuadEmitter emitter = context.getEmitter();
        List<RenderType> types = model.getRenderTypes(stack, false);
        //Item render types are entity types; map back to the chunk layer blend mode
        boolean translucent = types.stream().anyMatch(type -> type.toString().contains("translucent"));
        RenderMaterial material = material(translucent ? RenderType.translucent() : RenderType.cutout());
        for (Direction side : CULL_FACES) {
            for (BakedQuad quad : model.getQuads(null, side, randomSupplier.get(), ModelData.EMPTY, null)) {
                emitter.fromVanilla(quad, material, side);
                emitter.emit();
            }
        }
    }
}
