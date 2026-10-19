package mekanism.fabric.mixin.client;

import mekanism.neoshim.neoforge.client.event.RenderGuiLayerEvent;
import mekanism.neoshim.neoforge.client.gui.VanillaGuiLayers;
import mekanism.neoshim.neoforge.common.NeoForge;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's RenderGuiLayerEvent.Pre for the vanilla crosshair layer (hidden while a radial menu shows its back button). */
@Mixin(Gui.class)
public abstract class GuiCrosshairMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void mekanism$crosshairLayer(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (NeoForge.EVENT_BUS.post(new RenderGuiLayerEvent.Pre(guiGraphics, deltaTracker, VanillaGuiLayers.CROSSHAIR, (g, d) -> {
        })).isCanceled()) {
            ci.cancel();
        }
    }
}
