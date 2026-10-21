package mekanism.common.integration.crafttweaker;

import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import mekanism.neoshim.bus.api.EventPriority;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.fml.common.Mod;
import mekanism.neoshim.neoforge.data.loading.DatagenModLoader;

@Mod(value = Mekanism.MODID, depends = MekanismHooks.CRAFT_TWEAKER_MOD_ID)
public class MekanismCrTIntegration {

    public MekanismCrTIntegration(IEventBus modEventBus) {
        if (!DatagenModLoader.isRunningDataGen()) {
            //Register our CrT listener at lowest priority to try and ensure they get later ids than our normal registries
            modEventBus.addListener(EventPriority.LOWEST, CrTContentUtils::registerCrTContent);
        }
    }
}