package mekanism.neoshim.neoforge.client.model.geometry;

import com.mojang.math.Transformation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class StandaloneGeometryBakingContext implements IGeometryBakingContext {

    public static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath("neoforge", "standalone");
    public static final StandaloneGeometryBakingContext INSTANCE = builder().build(LOCATION);

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(IGeometryBakingContext parent) {
        return new Builder(parent);
    }

    private final ResourceLocation modelName;
    private final Predicate<String> materialCheck;
    private final Function<String, Material> materialLookup;
    private final boolean isGui3d;
    private final boolean useBlockLight;
    private final boolean useAmbientOcclusion;
    private final ItemTransforms transforms;
    private final Transformation rootTransform;
    @Nullable
    private final ResourceLocation renderTypeHint;
    private final BiPredicate<String, Boolean> visibilityTest;

    private StandaloneGeometryBakingContext(ResourceLocation modelName, Predicate<String> materialCheck, Function<String, Material> materialLookup, boolean isGui3d,
          boolean useBlockLight, boolean useAmbientOcclusion, ItemTransforms transforms, Transformation rootTransform, @Nullable ResourceLocation renderTypeHint,
          BiPredicate<String, Boolean> visibilityTest) {
        this.modelName = modelName;
        this.materialCheck = materialCheck;
        this.materialLookup = materialLookup;
        this.isGui3d = isGui3d;
        this.useBlockLight = useBlockLight;
        this.useAmbientOcclusion = useAmbientOcclusion;
        this.transforms = transforms;
        this.rootTransform = rootTransform;
        this.renderTypeHint = renderTypeHint;
        this.visibilityTest = visibilityTest;
    }

    @Override
    public String getModelName() {
        return modelName.toString();
    }

    @Override
    public boolean hasMaterial(String name) {
        return materialCheck.test(name);
    }

    @Override
    public Material getMaterial(String name) {
        return materialLookup.apply(name);
    }

    @Override
    public boolean isGui3d() {
        return isGui3d;
    }

    @Override
    public boolean useBlockLight() {
        return useBlockLight;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return useAmbientOcclusion;
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }

    @Override
    public Transformation getRootTransform() {
        return rootTransform;
    }

    @Nullable
    @Override
    public ResourceLocation getRenderTypeHint() {
        return renderTypeHint;
    }

    @Override
    public boolean isComponentVisible(String component, boolean fallback) {
        return visibilityTest.test(component, fallback);
    }

    public static final class Builder {

        private static final Material NO_MATERIAL = new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation());
        private Predicate<String> materialCheck = $ -> false;
        private Function<String, Material> materialLookup = $ -> NO_MATERIAL;
        private boolean isGui3d = true;
        private boolean useBlockLight = true;
        private boolean useAmbientOcclusion = true;
        private ItemTransforms transforms = ItemTransforms.NO_TRANSFORMS;
        private Transformation rootTransform = Transformation.identity();
        @Nullable
        private ResourceLocation renderTypeHint;
        private BiPredicate<String, Boolean> visibilityTest = (c, def) -> def;

        private Builder() {
        }

        private Builder(IGeometryBakingContext parent) {
            this.materialCheck = parent::hasMaterial;
            this.materialLookup = parent::getMaterial;
            this.isGui3d = parent.isGui3d();
            this.useBlockLight = parent.useBlockLight();
            this.useAmbientOcclusion = parent.useAmbientOcclusion();
            this.transforms = parent.getTransforms();
            this.rootTransform = parent.getRootTransform();
            this.renderTypeHint = parent.getRenderTypeHint();
            this.visibilityTest = parent::isComponentVisible;
        }

        public Builder withMaterials(Map<String, Material> materials) {
            Map<String, Material> copy = new HashMap<>(materials);
            this.materialCheck = copy::containsKey;
            this.materialLookup = name -> copy.getOrDefault(name, NO_MATERIAL);
            return this;
        }

        public Builder withMaterial(String name, Material material) {
            Predicate<String> oldCheck = this.materialCheck;
            Function<String, Material> oldLookup = this.materialLookup;
            this.materialCheck = n -> n.equals(name) || oldCheck.test(n);
            this.materialLookup = n -> n.equals(name) ? material : oldLookup.apply(n);
            return this;
        }

        public Builder withGui3d(boolean isGui3d) {
            this.isGui3d = isGui3d;
            return this;
        }

        public Builder withUseBlockLight(boolean useBlockLight) {
            this.useBlockLight = useBlockLight;
            return this;
        }

        public Builder withUseAmbientOcclusion(boolean useAmbientOcclusion) {
            this.useAmbientOcclusion = useAmbientOcclusion;
            return this;
        }

        public Builder withTransforms(ItemTransforms transforms) {
            this.transforms = transforms;
            return this;
        }

        public Builder withRootTransform(Transformation rootTransform) {
            this.rootTransform = rootTransform;
            return this;
        }

        public Builder withRenderTypeHint(ResourceLocation renderTypeHint) {
            this.renderTypeHint = renderTypeHint;
            return this;
        }

        public Builder withVisibleComponents(Object2BooleanMapLike visibility) {
            this.visibilityTest = visibility::getOrDefault;
            return this;
        }

        public StandaloneGeometryBakingContext build(ResourceLocation modelName) {
            return new StandaloneGeometryBakingContext(modelName, materialCheck, materialLookup, isGui3d, useBlockLight, useAmbientOcclusion, transforms,
                  rootTransform, renderTypeHint, visibilityTest);
        }
    }

    @FunctionalInterface
    public interface Object2BooleanMapLike {

        boolean getOrDefault(String key, boolean defaultValue);
    }
}
