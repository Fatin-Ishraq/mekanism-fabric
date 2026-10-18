package mekanism.fabric.modules;

import mekanism.fabric.NeoModule;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.fml.ModContainer;
import mekanism.tools.client.ToolsClient;
import mekanism.tools.common.MekanismTools;

public class ToolsModule implements NeoModule {

    @Override
    public String neoModId() {
        return MekanismTools.MODID;
    }

    @Override
    public String fabricModId() {
        return "mekanismtools_fabric";
    }

    @Override
    public void construct(ModContainer container, IEventBus modBus) {
        new MekanismTools(container, modBus);
    }

    @Override
    public void constructClient(ModContainer container) {
        new ToolsClient(container);
    }

    @Override
    public String[] clientSubscribers() {
        return new String[]{"mekanism.tools.client.ToolsClientRegistration"};
    }
}
