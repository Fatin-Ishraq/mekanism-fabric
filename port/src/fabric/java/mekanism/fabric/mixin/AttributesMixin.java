package mekanism.fabric.mixin;

import mekanism.fabric.NeoAttributes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Registers NeoForge's extra attributes alongside vanilla's, while the attribute registry is still open. */
@Mixin(Attributes.class)
public abstract class AttributesMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void mekanism$registerNeoAttributes(CallbackInfo ci) {
        NeoAttributes.init();
    }
}
