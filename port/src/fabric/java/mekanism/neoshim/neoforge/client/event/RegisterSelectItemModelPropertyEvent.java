package mekanism.neoshim.neoforge.client.event;

import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.resources.Identifier;

public class RegisterSelectItemModelPropertyEvent extends Event implements IModBusEvent {

    public void register(Identifier id, SelectItemModelProperty.Type<?, ?> type) {
        SelectItemModelProperties.ID_MAPPER.put(id, type);
    }
}
