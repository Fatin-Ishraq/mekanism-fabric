package mekanism.fabric.mixin.client;

import java.util.Map;
import mekanism.neoshim.neoforge.client.extensions.IModelManagerExtension;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelManager.class)
public abstract class ModelManagerMixin implements IModelManagerExtension {

    @Unique
    private ModelBakery mekanism$modelBakery;

    @Inject(method = "loadModels", at = @At("HEAD"))
    private void mekanism$captureBakery(ProfilerFiller profiler, Map<ResourceLocation, AtlasSet.StitchResult> atlases, ModelBakery bakery,
          CallbackInfoReturnable<?> cir) {
        this.mekanism$modelBakery = bakery;
    }

    @Override
    public ModelBakery getModelBakery() {
        return mekanism$modelBakery;
    }
}
