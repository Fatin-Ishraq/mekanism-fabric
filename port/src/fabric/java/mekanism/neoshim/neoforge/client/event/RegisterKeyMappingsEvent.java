package mekanism.neoshim.neoforge.client.event;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class RegisterKeyMappingsEvent extends Event implements IModBusEvent {

    public void register(KeyMapping key) {
        KeyBindingHelper.registerKeyBinding(key);
    }
}
