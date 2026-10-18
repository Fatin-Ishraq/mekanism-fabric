package mekanism.fabric.modules;

import mekanism.fabric.NeoModule;
import mekanism.generators.client.GeneratorsClient;
import mekanism.generators.common.MekanismGenerators;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.fml.ModContainer;

public class GeneratorsModule implements NeoModule {

    @Override
    public String neoModId() {
        return MekanismGenerators.MODID;
    }

    @Override
    public String fabricModId() {
        return "mekanismgenerators_fabric";
    }

    @Override
    public void construct(ModContainer container, IEventBus modBus) {
        new MekanismGenerators(container, modBus);
    }

    @Override
    public void constructClient(ModContainer container) {
        new GeneratorsClient(container);
    }

    @Override
    public String[] clientSubscribers() {
        return new String[]{"mekanism.generators.client.GeneratorsClientRegistration"};
    }
}
