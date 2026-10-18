package mekanism.fabric.client.bridge;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
import java.util.List;
import mekanism.fabric.client.HudHeights;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.fml.ModContainer;
import mekanism.neoshim.fml.event.lifecycle.FMLClientSetupEvent;
import mekanism.neoshim.neoforge.client.event.ClientPlayerNetworkEvent;
import mekanism.neoshim.neoforge.client.event.ClientTickEvent;
import mekanism.neoshim.neoforge.client.event.EntityRenderersEvent;
import mekanism.neoshim.neoforge.client.event.ModelEvent;
import mekanism.neoshim.neoforge.client.event.RegisterClientReloadListenersEvent;
import mekanism.neoshim.neoforge.client.event.RegisterColorHandlersEvent;
import mekanism.neoshim.neoforge.client.event.RegisterGuiLayersEvent;
import mekanism.neoshim.neoforge.client.event.RegisterItemDecorationsEvent;
import mekanism.neoshim.neoforge.client.event.RegisterKeyMappingsEvent;
import mekanism.neoshim.neoforge.client.event.RegisterMenuScreensEvent;
import mekanism.neoshim.neoforge.client.event.RegisterParticleProvidersEvent;
import mekanism.neoshim.neoforge.client.event.RenderLevelStageEvent;
import mekanism.neoshim.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import mekanism.neoshim.neoforge.client.gui.IConfigScreenFactory;
import mekanism.neoshim.neoforge.common.NeoForge;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Client half of the event bridge. Model/shader hooks are phase 6 (NOTES.md "Hook status"). */
public final class ClientBridge {

    private static List<RegisterGuiLayersEvent.Entry> guiLayers = List.of();

    private ClientBridge() {
    }

    public static void init(ModContainer container) {
        IEventBus modBus = container.getEventBus();
        IEventBus bus = NeoForge.EVENT_BUS;

        // Mod-bus registration events (NeoForge fires these during client mod loading), for every NeoForge-style mod in the port
        List<RegisterGuiLayersEvent.Entry> allLayers = new java.util.ArrayList<>();
        for (ModContainer mod : mekanism.fabric.bridge.ModBusLifecycle.mods()) {
            IEventBus bus0 = mod.getEventBus();
            bus0.post(new RegisterKeyMappingsEvent());
            bus0.post(new RegisterMenuScreensEvent());
            bus0.post(new EntityRenderersEvent.RegisterLayerDefinitions());
            bus0.post(new EntityRenderersEvent.RegisterRenderers());
            bus0.post(new RegisterParticleProvidersEvent());
            bus0.post(new RegisterClientExtensionsEvent());
            bus0.post(new RegisterItemDecorationsEvent());
            RegisterGuiLayersEvent layers = new RegisterGuiLayersEvent();
            bus0.post(layers);
            allLayers.addAll(layers.getLayers());
            mod.getCustomExtension(IConfigScreenFactory.class).ifPresent(factory ->
                  ConfigScreenFactoryRegistry.INSTANCE.register(mod.getModId(), (modId, parent) -> factory.createScreen(mod, parent)));
        }
        guiLayers = List.copyOf(allLayers);
        mekanism.fabric.client.model.ModelBridge.init();
        net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback.EVENT.register(shaderContext -> {
            for (ModContainer mod : mekanism.fabric.bridge.ModBusLifecycle.mods()) {
                try {
                    mod.getEventBus().post(new mekanism.neoshim.neoforge.client.event.RegisterShadersEvent(shaderContext));
                } catch (RuntimeException e) {
                    mekanism.common.Mekanism.logger.error("[Fabric port] Failed to register shaders for {}", mod.getModId(), e);
                }
            }
        });
        registerFluidRenderHandlers();
        for (ModContainer mod : mekanism.fabric.bridge.ModBusLifecycle.mods()) {
            mod.getEventBus().post(new FMLClientSetupEvent(mod));
        }

        if (Boolean.getBoolean("mekanism_fabric.screenshots")) {
            mekanism.fabric.client.ScreenshotDriver.init();
        }
        if (Boolean.getBoolean("mekanism_fabric.smokeWorld")) {
            mekanism.fabric.client.SmokeWorldDriver.init();
        }
        // Dev-only automated boot check (gradlew runSmokeClient): quit once the title screen is reached, no interaction
        if (Boolean.getBoolean("mekanism_fabric.smokeTest")) {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (client.getOverlay() == null && client.screen != null) {
                    mekanism.common.Mekanism.logger.info("[Fabric port] Smoke test: resource loading finished, showing {}; stopping client",
                          client.screen.getClass().getSimpleName());
                    logMissingModels(client);
                    //Force every mixin to apply now so broken injectors fail the smoke test instead of a later play session
                    org.spongepowered.asm.mixin.MixinEnvironment.getCurrentEnvironment().audit();
                    mekanism.common.Mekanism.logger.info("[Fabric port] Smoke test: mixin audit complete");
                    client.stop();
                }
            });
        }

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(mekanism.fabric.datamaps.DataMapLoader.SyncPayload.TYPE,
              (payload, context) -> mekanism.fabric.datamaps.DataMapLoader.applySync(payload.data(), context.client().getConnection().registryAccess()));


        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(mekanism.fabric.menu.ExtendedMenus.MenuDataPayload.TYPE,
              (payload, context) -> mekanism.fabric.menu.ExtendedMenus.setPendingClientData(payload.data()));
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(mekanism.fabric.entity.ComplexSpawns.SpawnDataPayload.TYPE,
              (payload, context) -> mekanism.fabric.entity.ComplexSpawns.apply(context.client().level, context.client().getConnection().registryAccess(), payload));

        // Game-bus bridges
        ClientTickEvents.START_CLIENT_TICK.register(client -> bus.post(new ClientTickEvent.Pre()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> bus.post(new ClientTickEvent.Post()));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
              bus.post(new ClientPlayerNetworkEvent.LoggingIn(client.gameMode, client.player, handler.getConnection())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
              bus.post(new ClientPlayerNetworkEvent.LoggingOut(client.gameMode, client.player, handler.getConnection())));
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> blockEntity.onLoad());
        ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                blockEntity.onChunkUnloaded();
            }
        });
        HudRenderCallback.EVENT.register((graphics, deltaTracker) -> {
            HudHeights.reset();
            if (!Minecraft.getInstance().options.hideGui) {
                for (RegisterGuiLayersEvent.Entry entry : guiLayers) {
                    entry.layer().render(graphics, deltaTracker);
                }
            }
        });
        WorldRenderEvents.BLOCK_OUTLINE.register((worldContext, outlineContext) -> {
            if (Minecraft.getInstance().hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                com.mojang.blaze3d.vertex.PoseStack poseStack = worldContext.matrixStack() != null ? worldContext.matrixStack() : new com.mojang.blaze3d.vertex.PoseStack();
                return !mekanism.neoshim.neoforge.client.ClientHooks.onDrawHighlight(worldContext.worldRenderer(), worldContext.camera(), blockHit,
                      worldContext.tickCounter(), poseStack, worldContext.consumers());
            }
            return true;
        });
        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT.register((client, screen, width, height) ->
              net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, partialTick) ->
                    bus.post(new mekanism.neoshim.neoforge.client.event.ScreenEvent.Render.Post(s, graphics, mouseX, mouseY, partialTick))));
        WorldRenderEvents.AFTER_ENTITIES.register(context -> postStage(RenderLevelStageEvent.Stage.AFTER_ENTITIES, context));
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> postStage(RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS, context));
        WorldRenderEvents.LAST.register(context -> postStage(RenderLevelStageEvent.Stage.AFTER_LEVEL, context));
    }

    /** Called from MinecraftInitMixin right before the initial resource reload (GL context, texture manager and colors exist). */
    public static void onBeforeInitialReload(Minecraft minecraft, net.minecraft.server.packs.resources.ReloadableResourceManager resourceManager) {
        for (ModContainer container : mekanism.fabric.bridge.ModBusLifecycle.mods()) {
            IEventBus modBus = container.getEventBus();
            modBus.post(new RegisterColorHandlersEvent.Block(minecraft.getBlockColors()));
            modBus.post(new RegisterColorHandlersEvent.Item(minecraft.itemColors, minecraft.getBlockColors()));
            RegisterClientReloadListenersEvent reloadListeners = new RegisterClientReloadListenersEvent();
            modBus.post(reloadListeners);
            reloadListeners.getListeners().forEach(resourceManager::registerReloadListener);
        }
    }

    /** World rendering of fluids with NeoForge client fluid type extensions (textures + tint). */
    private static void registerFluidRenderHandlers() {
        for (net.minecraft.world.level.material.Fluid fluid : net.minecraft.core.registries.BuiltInRegistries.FLUID) {
            if (!(fluid instanceof mekanism.neoshim.neoforge.fluids.BaseFlowingFluid flowing)) {
                continue;
            }
            mekanism.neoshim.neoforge.client.extensions.common.IClientFluidTypeExtensions extensions =
                  mekanism.neoshim.neoforge.client.extensions.common.RegisterClientExtensionsEvent.FLUID_TYPES.get(flowing.getFluidType());
            if (extensions == null || extensions.getStillTexture() == null) {
                continue;
            }
            net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry.INSTANCE.register(fluid,
                  new net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler(extensions.getStillTexture(), extensions.getFlowingTexture(),
                        extensions.getOverlayTexture(), extensions.getTintColor()));
        }
    }

    /** Smoke-test diagnostic: counts Mekanism block states / items whose model baked to the missing model. */
    private static void logMissingModels(Minecraft client) {
        var missing = client.getModelManager().getMissingModel();
        java.util.List<String> missingBlocks = new java.util.ArrayList<>();
        int blockStates = 0;
        for (var block : net.minecraft.core.registries.BuiltInRegistries.BLOCK) {
            var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
            if (!id.getNamespace().startsWith("mekanism")) {
                continue;
            }
            for (var state : block.getStateDefinition().getPossibleStates()) {
                blockStates++;
                if (client.getBlockRenderer().getBlockModel(state) == missing && state.getRenderShape() == net.minecraft.world.level.block.RenderShape.MODEL) {
                    missingBlocks.add(state.toString());
                }
            }
        }
        java.util.List<String> missingItems = new java.util.ArrayList<>();
        int items = 0;
        for (var item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
            var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
            if (!id.getNamespace().startsWith("mekanism")) {
                continue;
            }
            items++;
            if (client.getItemRenderer().getModel(new net.minecraft.world.item.ItemStack(item), null, null, 0) == missing) {
                missingItems.add(id.toString());
            }
        }
        mekanism.common.Mekanism.logger.info("[Fabric port] Smoke test models: {}/{} block states missing, {}/{} items missing", missingBlocks.size(), blockStates,
              missingItems.size(), items);
        missingBlocks.stream().limit(40).forEach(s -> mekanism.common.Mekanism.logger.warn("[Fabric port] Missing block model: {}", s));
        missingItems.stream().limit(80).forEach(s -> mekanism.common.Mekanism.logger.warn("[Fabric port] Missing item model: {}", s));
    }

    private static void postStage(RenderLevelStageEvent.Stage stage, WorldRenderContext context) {
        com.mojang.blaze3d.vertex.PoseStack poseStack = context.matrixStack() != null ? context.matrixStack() : new com.mojang.blaze3d.vertex.PoseStack();
        NeoForge.EVENT_BUS.post(new RenderLevelStageEvent(stage, context.worldRenderer(), poseStack, context.positionMatrix(), context.projectionMatrix(),
              context.worldRenderer().getTicks(), context.tickCounter(), context.camera(), context.frustum()));
    }

}
