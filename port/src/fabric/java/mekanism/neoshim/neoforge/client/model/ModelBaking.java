package mekanism.neoshim.neoforge.client.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;

/** NeoForge's {@code SimpleModelWrapper.bake(ModelBaker, ResolvedModel, ModelState)} overload, which 26.1 vanilla dropped. */
public final class ModelBaking {

    private ModelBaking() {
    }

    public static BlockStateModelPart bake(ModelBaker baker, ResolvedModel model, ModelState state) {
        TextureSlots textureSlots = model.getTopTextureSlots();
        boolean hasAmbientOcclusion = model.getTopAmbientOcclusion();
        Material.Baked particleMaterial = model.resolveParticleMaterial(textureSlots, baker);
        QuadCollection geometry = model.bakeTopGeometry(textureSlots, baker, state);
        Multimap<Identifier, Identifier> forbiddenSprites = null;
        for (BakedQuad bakedQuad : geometry.getAll()) {
            TextureAtlasSprite sprite = bakedQuad.materialInfo().sprite();
            if (!sprite.atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS)) {
                if (forbiddenSprites == null) {
                    forbiddenSprites = HashMultimap.create();
                }
                forbiddenSprites.put(sprite.atlasLocation(), sprite.contents().name());
            }
        }
        if (forbiddenSprites != null) {
            return baker.missingBlockModelPart();
        }
        return new SimpleModelWrapper(geometry, hasAmbientOcclusion, particleMaterial);
    }
}
