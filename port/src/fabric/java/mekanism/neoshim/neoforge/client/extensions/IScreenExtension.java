package mekanism.neoshim.neoforge.client.extensions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Injected into {@link Screen}. */
public interface IScreenExtension {

    default Minecraft getMinecraft() {
        return ((Screen) this).minecraft;
    }
}
