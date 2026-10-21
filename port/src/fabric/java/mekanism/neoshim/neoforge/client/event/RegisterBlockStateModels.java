package mekanism.neoshim.neoforge.client.event;

import com.mojang.serialization.MapCodec;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import mekanism.neoshim.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.minecraft.resources.Identifier;

/** Registers custom block state model types (the {@code type} field in blockstate JSON) with Fabric's model loading API. */
public class RegisterBlockStateModels extends Event implements IModBusEvent {

    public void registerModel(Identifier id, MapCodec<? extends CustomUnbakedBlockStateModel> codec) {
        net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel.register(id, codec);
    }
}
