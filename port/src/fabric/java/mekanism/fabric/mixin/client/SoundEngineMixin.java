package mekanism.fabric.mixin.client;

import mekanism.fabric.bridge.ModBusLifecycle;
import mekanism.neoshim.fml.ModContainer;
import mekanism.neoshim.neoforge.client.ClientHooks;
import mekanism.neoshim.neoforge.client.event.sound.SoundEngineLoadEvent;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's PlaySoundEvent (used by Mekanism's muffling upgrade) and SoundEngineLoadEvent. */
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    @Unique
    private boolean mekanism$replaying;

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void mekanism$playSound(SoundInstance sound, CallbackInfo ci) {
        if (mekanism$replaying) {
            return;
        }
        SoundInstance result = ClientHooks.playSound((SoundEngine) (Object) this, sound);
        if (result != sound) {
            ci.cancel();
            if (result != null) {
                mekanism$replaying = true;
                try {
                    ((SoundEngine) (Object) this).play(result);
                } finally {
                    mekanism$replaying = false;
                }
            }
        }
    }

    @Inject(method = "loadLibrary", at = @At("TAIL"))
    private void mekanism$loaded(CallbackInfo ci) {
        for (ModContainer mod : ModBusLifecycle.mods()) {
            mod.getEventBus().post(new SoundEngineLoadEvent((SoundEngine) (Object) this));
        }
    }
}
