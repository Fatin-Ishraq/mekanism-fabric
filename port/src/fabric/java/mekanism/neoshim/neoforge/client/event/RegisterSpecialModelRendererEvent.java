package mekanism.neoshim.neoforge.client.event;

import com.mojang.serialization.MapCodec;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;

public class RegisterSpecialModelRendererEvent extends Event implements IModBusEvent {

    public void register(Identifier id, MapCodec<? extends SpecialModelRenderer.Unbaked<?>> codec) {
        SpecialModelRenderers.ID_MAPPER.put(id, codec);
    }
}
