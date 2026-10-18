package mekanism.common.integration.jsonthings;

import dev.gigaherz.jsonthings.things.parsers.ThingResourceManager;
import mekanism.neoshim.bus.api.IEventBus;

public class JsonThingsIntegration {

    public static void hook(IEventBus bus) {
        ThingResourceManager.instance().registerParser(new JsonChemicalParser(bus));
    }
}