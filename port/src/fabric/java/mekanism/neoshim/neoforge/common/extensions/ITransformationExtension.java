package mekanism.neoshim.neoforge.common.extensions;

import com.mojang.math.Transformation;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Injected into {@link Transformation}. */
public interface ITransformationExtension {

    private Transformation self() {
        return (Transformation) (Object) this;
    }

    default boolean isIdentity() {
        return self().equals(Transformation.identity());
    }

    default Direction rotateTransform(Direction facing) {
        Vector3f dir = facing.step();
        Vector4f vec = new Vector4f(dir, 0);
        Matrix4f matrix = self().getMatrix();
        matrix.transform(vec);
        return Direction.getNearest(vec.x(), vec.y(), vec.z());
    }

    default Transformation blockCenterToCorner() {
        return self();
    }
}
