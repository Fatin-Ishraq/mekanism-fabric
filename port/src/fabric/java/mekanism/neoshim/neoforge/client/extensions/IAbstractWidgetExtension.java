package mekanism.neoshim.neoforge.client.extensions;

import net.minecraft.client.gui.components.AbstractWidget;

/** Injected into {@link AbstractWidget}; provides the NeoForge packed foreground color that 26.1 vanilla lacks. */
public interface IAbstractWidgetExtension {

    default int getFGColor() {
        return ((AbstractWidget) this).active ? 0xFFFFFF : 0xA0A0A0;
    }
}
