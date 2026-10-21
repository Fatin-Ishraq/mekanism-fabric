package mekanism.neoshim.neoforge.client.event;

import com.mojang.serialization.MapCodec;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.resources.Identifier;

public class RegisterConditionalItemModelPropertyEvent extends Event implements IModBusEvent {

    public void register(Identifier id, MapCodec<? extends ConditionalItemModelProperty> codec) {
        ConditionalItemModelProperties.ID_MAPPER.put(id, codec);
    }
}
