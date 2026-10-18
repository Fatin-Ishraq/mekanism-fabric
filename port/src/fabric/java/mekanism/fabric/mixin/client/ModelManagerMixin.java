package mekanism.fabric.mixin.client;

import mekanism.neoshim.neoforge.client.extensions.IModelManagerExtension;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps the model bakery of the last reload, which NeoForge exposes through the model manager. */
@Mixin(ModelManager.class)
public abstract class ModelManagerMixin implements IModelManagerExtension {

    @Unique
    private static volatile ModelBakery mekanism$lastBakery;

    @Inject(method = "loadModels", at = @At("HEAD"))
    private static void mekanism$captureBakery(CallbackInfoReturnable<?> cir, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) ModelBakery bakery) {
        mekanism$lastBakery = bakery;
    }

    @Override
    public ModelBakery getModelBakery() {
        return mekanism$lastBakery;
    }
}
