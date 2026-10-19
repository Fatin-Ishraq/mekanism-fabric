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
        return self().equals(Transformation.IDENTITY);
    }

    default void transformPosition(Vector4f position) {
        self().getMatrix().transform(position);
    }

    default void transformNormal(Vector3f normal) {
        self().getMatrix().transformDirection(normal);
        normal.normalize();
    }

    default Direction rotateTransform(Direction facing) {
        Vector3f dir = facing.step();
        Vector4f vec = new Vector4f(dir, 0);
        self().getMatrix().transform(vec);
        return Direction.getApproximateNearest(vec.x(), vec.y(), vec.z());
    }

    default Transformation blockCenterToCorner() {
        return applyOrigin(new Vector3f(0.5F, 0.5F, 0.5F));
    }

    default Transformation blockCornerToCenter() {
        return applyOrigin(new Vector3f(-0.5F, -0.5F, -0.5F));
    }

    /** The same transformation, applied around {@code origin} instead of the coordinate origin. */
    default Transformation applyOrigin(Vector3f origin) {
        if (isIdentity()) {
            return self();
        }
        Matrix4f matrix = new Matrix4f().translation(origin).mul(self().getMatrix()).translate(-origin.x(), -origin.y(), -origin.z());
        return new Transformation(matrix);
    }
}
