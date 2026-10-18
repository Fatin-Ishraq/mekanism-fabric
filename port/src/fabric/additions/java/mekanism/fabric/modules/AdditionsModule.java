package mekanism.fabric.modules;

import mekanism.additions.client.AdditionsClient;
import mekanism.additions.common.MekanismAdditions;
import mekanism.fabric.NeoModule;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.fml.ModContainer;

public class AdditionsModule implements NeoModule {

    @Override
    public String neoModId() {
        return MekanismAdditions.MODID;
    }

    @Override
    public String fabricModId() {
        return "mekanismadditions_fabric";
    }

    @Override
    public void construct(ModContainer container, IEventBus modBus) {
        new MekanismAdditions(container, modBus);
    }

    @Override
    public void constructClient(ModContainer container) {
        new AdditionsClient(container);
    }

    @Override
    public String[] clientSubscribers() {
        return new String[]{"mekanism.additions.client.AdditionsClientRegistration"};
    }
}
