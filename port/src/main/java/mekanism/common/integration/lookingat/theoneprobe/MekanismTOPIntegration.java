package mekanism.common.integration.lookingat.theoneprobe;

import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.fml.common.Mod;
import mekanism.neoshim.fml.event.lifecycle.InterModEnqueueEvent;

@Mod(value = Mekanism.MODID, depends = MekanismHooks.TOP_MOD_ID)
public class MekanismTOPIntegration {

    public MekanismTOPIntegration(IEventBus modEventBus) {
        modEventBus.addListener(InterModEnqueueEvent.class, event -> Mekanism.hooks.theOneProbe.sendImc("getTheOneProbe", TOPProvider::new));
    }
}