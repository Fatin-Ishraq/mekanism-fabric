package mekanism.fabric.client.model;

import java.util.ArrayList;
import java.util.List;
import mekanism.neoshim.neoforge.client.ChunkRenderTypeSet;
import mekanism.neoshim.neoforge.client.RenderTypeGroup;
import mekanism.neoshim.neoforge.client.model.IDynamicBakedModel;
import mekanism.neoshim.neoforge.client.model.data.ModelData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Baked composite model: quads of each part are reported under that part's render type. */
public final class CompositeBakedModel implements IDynamicBakedModel {

    public record Part(BakedModel model, RenderTypeGroup renderTypes) {
    }

    private final List<Part> parts;
    private final boolean ambientOcclusion;
    private final boolean gui3d;
    private final boolean blockLight;
    private final TextureAtlasSprite particle;
    private final ItemTransforms transforms;
    private final ItemOverrides overrides;

    public CompositeBakedModel(List<Part> parts, boolean ambientOcclusion, boolean gui3d, boolean blockLight, TextureAtlasSprite particle, ItemTransforms transforms,
          ItemOverrides overrides) {
        this.parts = parts;
        this.ambientOcclusion = ambientOcclusion;
        this.gui3d = gui3d;
        this.blockLight = blockLight;
        this.particle = particle;
        this.transforms = transforms;
        this.overrides = overrides;
    }

    @Nullable
    private static RenderType chunkType(Part part) {
        return part.renderTypes().isEmpty() ? null : part.renderTypes().block();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        List<BakedQuad> quads = new ArrayList<>();
        for (Part part : parts) {
            RenderType partType = chunkType(part);
            if (renderType == null) {
                quads.addAll(part.model().getQuads(state, side, rand, extraData, null));
            } else if (partType != null) {
                if (partType == renderType) {
                    quads.addAll(part.model().getQuads(state, side, rand, extraData, renderType));
                }
            } else if (state != null && part.model().getRenderTypes(state, rand, extraData).contains(renderType)) {
                quads.addAll(part.model().getQuads(state, side, rand, extraData, renderType));
            }
        }
        return quads;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        List<ChunkRenderTypeSet> sets = new ArrayList<>();
        for (Part part : parts) {
            RenderType type = chunkType(part);
            sets.add(type != null ? ChunkRenderTypeSet.of(type) : part.model().getRenderTypes(state, rand, data));
        }
        return ChunkRenderTypeSet.union(sets);
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        List<RenderType> types = new ArrayList<>();
        for (Part part : parts) {
            if (!part.renderTypes().isEmpty()) {
                RenderType type = fabulous ? part.renderTypes().entityFabulous() : part.renderTypes().entity();
                if (!types.contains(type)) {
                    types.add(type);
                }
            }
        }
        return types.isEmpty() ? IDynamicBakedModel.super.getRenderTypes(itemStack, fabulous) : types;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return ambientOcclusion;
    }

    @Override
    public boolean isGui3d() {
        return gui3d;
    }

    @Override
    public boolean usesBlockLight() {
        return blockLight;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return particle;
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }
}
