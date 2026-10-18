package mekanism.tools.client;

import mekanism.tools.common.MekanismTools;
import mekanism.neoshim.api.distmarker.Dist;
import mekanism.neoshim.fml.ModContainer;
import mekanism.neoshim.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import mekanism.neoshim.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = MekanismTools.MODID, dist = Dist.CLIENT)
public class ToolsClient {

    public ToolsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, () -> (c, parent) -> new ConfigurationScreen(c.getModId(), parent));//FABRIC: FCAP screen ctor takes a mod id
    }
}