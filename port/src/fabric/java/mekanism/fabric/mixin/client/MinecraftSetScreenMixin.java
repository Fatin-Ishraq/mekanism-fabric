package mekanism.fabric.mixin.client;

import mekanism.neoshim.neoforge.client.event.ScreenEvent;
import mekanism.neoshim.neoforge.common.NeoForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's ScreenEvent.Opening. */
@Mixin(Minecraft.class)
public abstract class MinecraftSetScreenMixin {

    @Shadow
    @Nullable
    public Screen screen;
    @Unique
    private boolean mekanism$redirecting;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void mekanism$opening(@Nullable Screen newScreen, CallbackInfo ci) {
        if (newScreen == null || mekanism$redirecting) {
            return;
        }
        ScreenEvent.Opening event = NeoForge.EVENT_BUS.post(new ScreenEvent.Opening(screen, newScreen));
        if (event.isCanceled()) {
            ci.cancel();
        } else if (event.getNewScreen() != newScreen) {
            ci.cancel();
            mekanism$redirecting = true;
            try {
                ((Minecraft) (Object) this).setScreen(event.getNewScreen());
            } finally {
                mekanism$redirecting = false;
            }
        }
    }
}
