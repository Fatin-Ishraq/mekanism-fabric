package mekanism.neoshim.neoforge.client.extensions;

import net.minecraft.client.gui.components.AbstractWidget;

/** Injected into {@link AbstractWidget}: NeoForge's foreground colour hook (subclasses that need a settable colour keep their own field). */
public interface IAbstractWidgetExtension {

    default int getFGColor() {
        return ((AbstractWidget) this).active ? 0xFFFFFFFF : 0xFFA0A0A0;
    }
}
