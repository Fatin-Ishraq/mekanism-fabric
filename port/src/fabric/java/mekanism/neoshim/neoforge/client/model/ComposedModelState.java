package mekanism.neoshim.neoforge.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.core.Direction;
import org.joml.Matrix4fc;

/** A model state whose transformation is the parent's composed with an extra transformation. Face transformations come from the parent. */
public record ComposedModelState(ModelState parent, Transformation transformation) implements ModelState {

    public ComposedModelState(ModelState parent, Transformation transformation) {
        this.parent = parent;
        this.transformation = parent.transformation().compose(transformation);
    }

    @Override
    public Matrix4fc faceTransformation(Direction direction) {
        return parent.faceTransformation(direction);
    }

    @Override
    public Matrix4fc inverseFaceTransformation(Direction direction) {
        return parent.inverseFaceTransformation(direction);
    }
}
