package mekanism.fabric.mixin.client;

import mekanism.neoshim.neoforge.client.extensions.IAbstractWidgetExtension;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;

/** Applies {@link IAbstractWidgetExtension} at runtime. */
@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin implements IAbstractWidgetExtension {
}
