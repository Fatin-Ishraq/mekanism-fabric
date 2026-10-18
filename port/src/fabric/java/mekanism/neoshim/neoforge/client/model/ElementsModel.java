package mekanism.neoshim.neoforge.client.model;

import java.util.List;
import java.util.function.Function;
import mekanism.neoshim.neoforge.client.model.geometry.IGeometryBakingContext;
import mekanism.neoshim.neoforge.client.model.geometry.IUnbakedGeometry;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;

/** Geometry made of vanilla block elements ("neoforge:elements"). */
public class ElementsModel implements IUnbakedGeometry<ElementsModel> {

    private final List<BlockElement> elements;

    public ElementsModel(List<BlockElement> elements) {
        this.elements = elements;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
          ItemOverrides overrides) {
        TextureAtlasSprite particle = spriteGetter.apply(context.getMaterial("particle"));
        SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(context.useAmbientOcclusion(), context.useBlockLight(), context.isGui3d(),
              context.getTransforms(), overrides).particle(particle);
        for (BlockElement element : elements) {
            for (Direction direction : element.faces.keySet()) {
                BlockElementFace face = element.faces.get(direction);
                TextureAtlasSprite sprite = spriteGetter.apply(context.getMaterial(face.texture()));
                var quad = net.minecraft.client.renderer.block.model.BlockModel.bakeFace(element, face, sprite, direction, modelState);
                if (face.cullForDirection() == null) {
                    builder.addUnculledFace(quad);
                } else {
                    builder.addCulledFace(Direction.rotate(modelState.getRotation().getMatrix(), face.cullForDirection()), quad);
                }
            }
        }
        return builder.build();
    }
}
