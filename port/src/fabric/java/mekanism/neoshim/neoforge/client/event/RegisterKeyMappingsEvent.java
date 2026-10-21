package mekanism.neoshim.neoforge.client.event;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

public class RegisterKeyMappingsEvent extends Event implements IModBusEvent {

    /** Fabric registers a key mapping's category together with the mapping itself, so this only preserves the NeoForge call shape. */
    public void registerCategory(KeyMapping.Category category) {
    }

    public void register(KeyMapping key) {
        KeyMappingHelper.registerKeyMapping(key);
    }
}
