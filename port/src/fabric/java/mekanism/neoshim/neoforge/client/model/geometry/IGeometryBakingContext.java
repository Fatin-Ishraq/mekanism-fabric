package mekanism.neoshim.neoforge.client.model.geometry;

import com.mojang.math.Transformation;
import mekanism.neoshim.neoforge.client.RenderTypeGroup;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface IGeometryBakingContext {

    String getModelName();

    boolean hasMaterial(String name);

    Material getMaterial(String name);

    boolean isGui3d();

    boolean useBlockLight();

    boolean useAmbientOcclusion();

    ItemTransforms getTransforms();

    Transformation getRootTransform();

    @Nullable
    ResourceLocation getRenderTypeHint();

    boolean isComponentVisible(String component, boolean fallback);

    default RenderTypeGroup getRenderType(ResourceLocation name) {
        return mekanism.fabric.client.model.NamedRenderTypes.get(name);
    }
}
