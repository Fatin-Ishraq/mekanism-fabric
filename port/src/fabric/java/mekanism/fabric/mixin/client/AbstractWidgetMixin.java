package mekanism.fabric.mixin.client;

import mekanism.neoshim.neoforge.client.extensions.IAbstractWidgetExtension;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;

/** Carries the {@link IAbstractWidgetExtension} interface injection into {@link AbstractWidget}. */
@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin implements IAbstractWidgetExtension {
}
