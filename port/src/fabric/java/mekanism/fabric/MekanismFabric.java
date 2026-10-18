package mekanism.fabric;

import mekanism.common.Mekanism;
import mekanism.fabric.bridge.GameEventBridge;
import mekanism.fabric.bridge.ModBusLifecycle;
import mekanism.fabric.compat.IngredientCodecs;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.fml.ModContainer;
import mekanism.neoshim.fml.ModList;
import net.fabricmc.api.ModInitializer;

/**
 * Fabric entrypoint for the core module. Constructs Mekanism exactly like FML would (mod container + mod event bus), then drives the
 * NeoForge mod-bus lifecycle (registration, capabilities, payloads, common setup, IMC) in NeoForge's order. See NOTES.md D2/D3.
 */
public class MekanismFabric implements ModInitializer {

    public static final String FABRIC_MOD_ID = "mekanism_fabric";
    private static ModContainer container;

    public static ModContainer container() {
        return container;
    }

    @Override
    public void onInitialize() {
        IEventBus modBus = ModContainer.createModBus(Mekanism.MODID);
        container = new ModContainer(Mekanism.MODID, FABRIC_MOD_ID, modBus);
        ModList.get().registerContainer(container);
        ModBusLifecycle.addMod(container);

        IngredientCodecs.registerNeoForgeIngredientTypes();
        GameEventBridge.init();
        mekanism.fabric.datamaps.DataMapLoader.init();
        mekanism.fabric.menu.ExtendedMenus.init();
        mekanism.fabric.compat.CapabilityBridge.registerFallbacks();

        new Mekanism(container, modBus);
        EventBusSubscribers.registerCommon(modBus);

        ModBusLifecycle.runCommonLifecycle();
        mekanism.fabric.compat.BiomeModifierBridge.apply(FABRIC_MOD_ID);
        logRegistrationSummary();
    }

    private static void logRegistrationSummary() {
        Mekanism.logger.info("[Fabric port] Registered {} blocks, {} items, {} block entity types, {} fluids, {} entity types, {} menus, {} chemicals",
              count(net.minecraft.core.registries.BuiltInRegistries.BLOCK), count(net.minecraft.core.registries.BuiltInRegistries.ITEM),
              count(net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE), count(net.minecraft.core.registries.BuiltInRegistries.FLUID),
              count(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE), count(net.minecraft.core.registries.BuiltInRegistries.MENU),
              count(mekanism.api.MekanismAPI.CHEMICAL_REGISTRY));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            long recipes = server.getRecipeManager().getRecipes().stream().filter(r -> r.id().getNamespace().equals(Mekanism.MODID)).count();
            Mekanism.logger.info("[Fabric port] Loaded {} mekanism recipes", recipes);
        });
    }

    private static long count(net.minecraft.core.Registry<?> registry) {
        return registry.keySet().stream().filter(id -> id.getNamespace().equals(Mekanism.MODID)).count();
    }
}
