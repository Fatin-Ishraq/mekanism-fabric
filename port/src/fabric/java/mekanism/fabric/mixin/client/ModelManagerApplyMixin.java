package mekanism.fabric.mixin.client;

import java.util.Map;
import mekanism.fabric.bridge.ModBusLifecycle;
import mekanism.neoshim.fml.ModContainer;
import mekanism.neoshim.neoforge.client.event.ModelEvent;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fires NeoForge's ModelEvent.BakingCompleted after the model manager applied a reload. */
@Mixin(ModelManager.class)
public abstract class ModelManagerApplyMixin {

    @Shadow
    private Map<ModelResourceLocation, BakedModel> bakedRegistry;

    @Inject(method = "apply", at = @At("TAIL"))
    private void mekanism$bakingCompleted(ModelManager.ReloadState reloadState, ProfilerFiller profiler, CallbackInfo ci) {
        ModelManager self = (ModelManager) (Object) this;
        for (ModContainer mod : ModBusLifecycle.mods()) {
            mod.getEventBus().post(new ModelEvent.BakingCompleted(self, bakedRegistry, self.getModelBakery()));
        }
    }
}
