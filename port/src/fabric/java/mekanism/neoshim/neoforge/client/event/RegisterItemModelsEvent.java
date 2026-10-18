package mekanism.neoshim.neoforge.client.event;

import com.mojang.serialization.MapCodec;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.resources.Identifier;

public class RegisterItemModelsEvent extends Event implements IModBusEvent {

    public void register(Identifier id, MapCodec<? extends ItemModel.Unbaked> codec) {
        ItemModels.ID_MAPPER.put(id, codec);
    }
}
