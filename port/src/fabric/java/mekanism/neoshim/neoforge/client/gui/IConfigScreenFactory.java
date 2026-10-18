package mekanism.neoshim.neoforge.client.gui;

import mekanism.neoshim.fml.IExtensionPoint;
import mekanism.neoshim.fml.ModContainer;
import net.minecraft.client.gui.screens.Screen;

public interface IConfigScreenFactory extends IExtensionPoint {

    Screen createScreen(ModContainer container, Screen modListScreen);
}
