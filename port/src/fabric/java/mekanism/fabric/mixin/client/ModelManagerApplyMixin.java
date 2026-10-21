package mekanism.fabric.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import mekanism.fabric.bridge.ModBusLifecycle;
import mekanism.neoshim.fml.ModContainer;
import mekanism.neoshim.neoforge.client.event.ModelEvent;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fires NeoForge's ModelEvent.BakingCompleted after the model manager applied a reload. */
@Mixin(ModelManager.class)
public abstract class ModelManagerApplyMixin {

    @Inject(method = "apply", at = @At("TAIL"))
    private void mekanism$bakingCompleted(@Coerce Object preparations, CallbackInfo ci, @Local ModelBakery.BakingResult bakedModels) {
        ModelManager self = (ModelManager) (Object) this;
        for (ModContainer mod : ModBusLifecycle.mods()) {
            mod.getEventBus().post(new ModelEvent.BakingCompleted(self, bakedModels, self.getModelBakery()));
        }
    }
}
