package mekanism.neoshim.neoforge.common.extensions;

import com.mojang.math.Transformation;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Injected into {@link Transformation}. Mirrors NeoForge's extension (applyOrigin etc. are NeoForge-patch methods, not vanilla). */
public interface ITransformationExtension {

    private Transformation self() {
        return (Transformation) (Object) this;
    }

    default boolean isIdentity() {
        return self().equals(Transformation.IDENTITY);
    }

    default void transformPosition(Vector4f position) {
        position.mul(self().getMatrix());
    }

    default void transformNormal(Vector3f normal) {
        normal.mul(self().getMatrixCopy().normal(new Matrix3f()));
        normal.normalize();
    }

    default Direction rotateTransform(Direction facing) {
        return Direction.rotate(self().getMatrix(), facing);
    }

    default Transformation blockCenterToCorner() {
        return applyOrigin(new Vector3f(0.5F, 0.5F, 0.5F));
    }

    default Transformation blockCornerToCenter() {
        return applyOrigin(new Vector3f(-0.5F, -0.5F, -0.5F));
    }

    /** Rotates/scales about the given origin: {@code T(origin) * M * T(-origin)}. */
    default Transformation applyOrigin(Vector3f origin) {
        Transformation self = self();
        if (self.isIdentity()) {
            return Transformation.IDENTITY;
        }
        Matrix4f result = new Matrix4f();
        new Matrix4f().translation(origin.x(), origin.y(), origin.z()).mul(self.getMatrix(), result);
        result.mul(new Matrix4f().translation(-origin.x(), -origin.y(), -origin.z()));
        return new Transformation(result);
    }
}
