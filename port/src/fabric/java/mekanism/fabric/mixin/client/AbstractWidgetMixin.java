package mekanism.fabric.mixin.client;

import mekanism.neoshim.neoforge.client.extensions.IAbstractWidgetExtension;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** NeoForge routes mouseClicked to {@code onClick(x, y, button)}; Mekanism widgets override that overload. */
@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin implements IAbstractWidgetExtension {

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/AbstractWidget;onClick(DD)V"))
    private void mekanism$onClickWithButton(AbstractWidget widget, double mouseX, double mouseY, double originalX, double originalY, int button) {
        widget.onClick(mouseX, mouseY, button);
    }
}
