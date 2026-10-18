package mekanism.neoshim.neoforge.client.extensions;

import net.minecraft.client.gui.components.AbstractWidget;

/** Injected into {@link AbstractWidget}; AbstractWidgetMixin routes mouseClicked to the button-aware onClick. */
public interface IAbstractWidgetExtension {

    default void onClick(double mouseX, double mouseY, int button) {
        ((AbstractWidget) this).onClick(mouseX, mouseY);
    }

    default int getFGColor() {
        return ((AbstractWidget) this).active ? 0xFFFFFF : 0xA0A0A0;
    }
}
