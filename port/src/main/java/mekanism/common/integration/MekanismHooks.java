package mekanism.common.integration;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.common.integration.computer.FactoryRegistry;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.recipe.bin.BinInsertRecipe;
import mekanism.common.registries.MekanismItems;
import net.minecraft.resources.ResourceLocation;
import mekanism.neoshim.bus.api.EventPriority;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.fml.InterModComms;
import mekanism.neoshim.fml.ModList;
import mekanism.neoshim.fml.event.lifecycle.InterModEnqueueEvent;
import mekanism.neoshim.neoforge.capabilities.RegisterCapabilitiesEvent;
import mekanism.neoshim.neoforge.common.NeoForge;
import mekanism.neoshim.neoforge.data.loading.DatagenModLoader;

/**
 * Hooks for Mekanism. Use to grab items or blocks out of different mods.
 *
 * @author AidanBrady
 */
public final class MekanismHooks {

    public record IntegrationInfo(String modid, boolean isLoaded) {

        private IntegrationInfo(String modid, Predicate<String> loadedCheck) {
            this(modid, loadedCheck.test(modid));
        }

        private void sendImc(String method, Supplier<?> toSend) {
            InterModComms.sendTo(modid, method, toSend);
        }

        public ResourceLocation rl(String path) {
            return ResourceLocation.fromNamespaceAndPath(modid, path);
        }

        public void assertLoaded() {
            if (!isLoaded) {
                throw new IllegalStateException(modid + " is not loaded");
            }
        }
    }

    public final IntegrationInfo computerCraft;
    public final IntegrationInfo craftTweaker;
    public final IntegrationInfo curios;
    public final IntegrationInfo darkModeEverywhere;
    public final IntegrationInfo emi;
    public final IntegrationInfo fluxNetworks;
    public final IntegrationInfo framedBlocks;
    public final IntegrationInfo genderMod;
    public final IntegrationInfo grandPower;
    public final IntegrationInfo jei;
    public final IntegrationInfo jeiTweaker;
    public final IntegrationInfo jsonThings;
    public final IntegrationInfo oc2;
    public final IntegrationInfo projecte;
    public final IntegrationInfo recipeStages;
    public final IntegrationInfo theOneProbe;

    //Note: These have to be static for use in CraftTweaker annotations
    public static final String JEITWEAKER_MOD_ID = "jeitweaker";
    public static final String PROJECTE_MOD_ID = "projecte";

    public MekanismHooks() {
        ModList modList = ModList.get();
        //Note: The modlist is null when running tests
        Predicate<String> loadedCheck = modList == null ? modid -> false : modList::isLoaded;
        computerCraft = new IntegrationInfo("computercraft", loadedCheck);
        craftTweaker = new IntegrationInfo("crafttweaker", loadedCheck);
        curios = new IntegrationInfo("curios", loadedCheck);
        darkModeEverywhere = new IntegrationInfo("darkmodeeverywhere", loadedCheck);
        fluxNetworks = new IntegrationInfo("fluxnetworks", loadedCheck);
        grandPower = new IntegrationInfo("grandpower", loadedCheck);
        jei = new IntegrationInfo("jei", loadedCheck);
        emi = new IntegrationInfo("emi", loadedCheck);
        jeiTweaker = new IntegrationInfo("jeitweaker", loadedCheck);
        jsonThings = new IntegrationInfo("jsonthings", loadedCheck);
        oc2 = new IntegrationInfo("oc2", loadedCheck);
        projecte = new IntegrationInfo("projecte", loadedCheck);
        recipeStages = new IntegrationInfo("recipestages", loadedCheck);
        theOneProbe = new IntegrationInfo("theoneprobe", loadedCheck);
        genderMod = new IntegrationInfo("wildfire_gender", loadedCheck);
        framedBlocks = new IntegrationInfo("framedblocks", loadedCheck);
    }

    public void hookConstructor(final IEventBus modEventBus) {
        if (curios.isLoaded()) {
            CuriosIntegration.addListeners(modEventBus);
        }
        //FABRIC: CraftTweaker, JsonThings, ProjectE and FramedBlocks hooks removed (NeoForge-only integrations, see NOTES.md)
    }

    public void hookCapabilityRegistration(RegisterCapabilitiesEvent event) {
        EnergyCompatUtils.initLoadedCache();
        //FABRIC: Wildfire's Gender Mod armor config removed (NeoForge capability based)
    }

    public void hookCommonSetup() {
        if (computerCompatEnabled()) {
            FactoryRegistry.load();
            //FABRIC: ComputerCraft API registration (CCCapabilityHelper) is TODO
        }

        //TODO - 1.20: Move this out of here and back to always being registered whenever it gets fixed in Neo.
        // Modifying the result doesn't apply properly when "quick crafting"
        if (ModList.get().isLoaded("fastbench")) {
            NeoForge.EVENT_BUS.addListener(BinInsertRecipe::onCrafting);
        }
    }

    public void sendIMCMessages(InterModEnqueueEvent event) {
        if (darkModeEverywhere.isLoaded()) {
            //Note: While it is only strings, so it is safe to call and IMC validates the mods are loaded
            // we add this check here, so we can skip iterating the list of things we want to blacklist when it is not present
            sendDarkModeEverywhereIMC();
        }
        //FABRIC: The One Probe IMC removed (NeoForge-only)
    }

    public boolean computerCompatEnabled() {
        return computerCraft.isLoaded() || oc2.isLoaded();
    }

    public boolean recipeViewerCompatEnabled() {
        return emi.isLoaded() || jei.isLoaded();
    }

    /**
     * @apiNote DME only uses strings in IMC, so we can safely just include them here without worrying about classloading issues
     */
    private void sendDarkModeEverywhereIMC() {
        List<Supplier<String>> methodBlacklist = List.of(
              //Used for drawing fluids and chemicals in various GUIs including JEI as well as similar styled things
              () -> "mekanism.client.gui.GuiUtils:drawTiledSprite",
              //MekaSuit HUD rendering (already configurable by the user)
              () -> "mekanism.client.render.HUDRenderer:renderCompass",
              () -> "mekanism.client.render.HUDRenderer:renderHUDElement"
        );
        for (Supplier<String> method : methodBlacklist) {
            darkModeEverywhere.sendImc("dme-shaderblacklist", method);
        }
    }
}
