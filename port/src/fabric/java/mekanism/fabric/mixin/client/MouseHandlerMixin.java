package mekanism.fabric.mixin.client;

import mekanism.neoshim.neoforge.client.event.InputEvent;
import mekanism.neoshim.neoforge.common.NeoForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's InputEvent.MouseScrollingEvent (in-world scrolling, e.g. mode switching while sneaking). */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private double xpos;
    @Shadow
    private double ypos;

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void mekanism$scroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
        if (window != minecraft.getWindow().handle() || minecraft.screen != null || minecraft.player == null) {
            return;
        }
        double sensitivity = minecraft.options.mouseWheelSensitivity().get();
        boolean discrete = minecraft.options.discreteMouseScroll().get();
        double deltaX = (discrete ? Math.signum(xOffset) : xOffset) * sensitivity;
        double deltaY = (discrete ? Math.signum(yOffset) : yOffset) * sensitivity;
        MouseHandler self = (MouseHandler) (Object) this;
        InputEvent.MouseScrollingEvent event = new InputEvent.MouseScrollingEvent(deltaX, deltaY, self.isLeftPressed(), self.isMiddlePressed(), self.isRightPressed(),
              xpos, ypos);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            ci.cancel();
        }
    }
}
