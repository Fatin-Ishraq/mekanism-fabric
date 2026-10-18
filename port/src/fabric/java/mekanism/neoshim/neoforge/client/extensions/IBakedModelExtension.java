package mekanism.neoshim.neoforge.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import mekanism.neoshim.neoforge.client.ChunkRenderTypeSet;
import mekanism.neoshim.neoforge.client.model.data.ModelData;
import mekanism.neoshim.neoforge.common.util.TriState;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link BakedModel}. Fabric rendering goes through the port's FRAPI bridge (NOTES.md "Hook status"). */
public interface IBakedModelExtension {

    private BakedModel self() {
        return (BakedModel) this;
    }

    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        return self().getQuads(state, side, rand);
    }

    default TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) {
        return self().useAmbientOcclusion() ? TriState.DEFAULT : TriState.FALSE;
    }

    default BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        self().getTransforms().getTransform(transformType).apply(applyLeftHandTransform, poseStack);
        return self();
    }

    default ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        return modelData;
    }

    default TextureAtlasSprite getParticleIcon(ModelData data) {
        return self().getParticleIcon();
    }

    default ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return ChunkRenderTypeSet.of(ItemBlockRenderTypes.getChunkRenderType(state));
    }

    default List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        return List.of(ItemBlockRenderTypes.getRenderType(itemStack, fabulous));
    }

    default List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        return List.of(self());
    }
}
