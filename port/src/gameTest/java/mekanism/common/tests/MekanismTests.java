package mekanism.common.tests;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.nio.file.Path;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.Identifier;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.fml.ModContainer;
import mekanism.neoshim.fml.common.Mod;
import mekanism.neoshim.neoforge.common.NeoForge;
import mekanism.neoshim.neoforge.event.RegisterCommandsEvent;
import mekanism.neoshim.testframework.conf.ClientConfiguration;
import mekanism.neoshim.testframework.conf.Feature;
import mekanism.neoshim.testframework.conf.FrameworkConfiguration;
import mekanism.neoshim.testframework.impl.MutableTestFramework;
import mekanism.neoshim.testframework.summary.GitHubActionsStepSummaryDumper;
import mekanism.neoshim.testframework.summary.JUnitSummaryDumper;

@Mod(MekanismTests.MODID)
public class MekanismTests {

    public static final String MODID = "mekanismtests";

    public MekanismTests(IEventBus modBus, ModContainer container) {
        //More or less a copy of mekanism.neoshim.neoforge.eventtest.internal.TestsMod but with a few tweaks
        final MutableTestFramework framework = FrameworkConfiguration.builder(rl("tests"))
              .clientConfiguration(() -> ClientConfiguration.builder()
                    .toggleOverlayKey(InputConstants.KEY_O)
                    .openManagerKey(InputConstants.KEY_M)
                    .build())
              .enable(Feature.CLIENT_SYNC, Feature.TEST_STORE)
              //TODO: Figure out which dumpers we want to enable and how they work
              .dumpers(new JUnitSummaryDumper(Path.of("gameTest/")), new GitHubActionsStepSummaryDumper())
              .build().create();

        framework.init(modBus, container);

        NeoForge.EVENT_BUS.addListener((final RegisterCommandsEvent event) -> {
            final LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("tests");
            framework.registerCommands(node);
            event.getDispatcher().register(node);
        });
    }

    public static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}