package mekanism.neoshim.neoforge.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import org.joml.Vector3f;

/** Helper methods used by model loaders that NeoForge provides. Only the transform composition is needed by Mekanism's model loaders. */
public final class UnbakedElementsHelper {

    private static final Vector3f CENTER = new Vector3f(-0.5F, -0.5F, -0.5F);

    private UnbakedElementsHelper() {
    }

    public static ModelState composeRootTransformIntoModelState(ModelState state, Transformation transformation) {
        if (transformation.isIdentity()) {
            return state;
        }
        return new ComposedModelState(state, transformation.applyOrigin(CENTER));
    }
}
