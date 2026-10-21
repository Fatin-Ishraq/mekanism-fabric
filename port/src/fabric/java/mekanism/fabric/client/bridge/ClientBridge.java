package mekanism.fabric.client.bridge;

import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;
import java.util.List;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.fml.ModContainer;
import mekanism.neoshim.fml.event.lifecycle.FMLClientSetupEvent;
import mekanism.neoshim.neoforge.client.event.AddClientReloadListenersEvent;
import mekanism.neoshim.neoforge.client.event.ClientPlayerNetworkEvent;
import mekanism.neoshim.neoforge.client.event.ClientTickEvent;
import mekanism.neoshim.neoforge.client.event.EntityRenderersEvent;
import mekanism.neoshim.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import mekanism.neoshim.neoforge.client.event.ModelEvent;
import mekanism.neoshim.neoforge.client.event.RecipesReceivedEvent;
import mekanism.neoshim.neoforge.client.event.RegisterFluidModelsEvent;
import mekanism.neoshim.neoforge.client.event.RegisterTextureAtlasesEvent;
import mekanism.neoshim.neoforge.client.event.RegisterColorHandlersEvent;
import mekanism.neoshim.neoforge.client.event.RegisterGuiLayersEvent;
import mekanism.neoshim.neoforge.client.event.RenderGuiLayerEvent;
import mekanism.neoshim.neoforge.client.event.RegisterItemDecorationsEvent;
import mekanism.neoshim.neoforge.client.event.RegisterKeyMappingsEvent;
import mekanism.neoshim.neoforge.client.event.RegisterMenuScreensEvent;
import mekanism.neoshim.neoforge.client.event.RegisterParticleProvidersEvent;
import mekanism.neoshim.neoforge.client.event.RenderLevelStageEvent;
import mekanism.neoshim.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import mekanism.neoshim.neoforge.client.gui.GuiLayer;
import mekanism.neoshim.neoforge.client.gui.VanillaGuiLayers;
import mekanism.neoshim.neoforge.client.gui.IConfigScreenFactory;
import mekanism.neoshim.neoforge.common.NeoForge;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ExtractItemDecorationsCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Client half of the event bridge. Model/shader hooks are phase 6 (NOTES.md "Hook status"). */
public final class ClientBridge {

    private ClientBridge() {
    }

    public static void init(ModContainer container) {
        IEventBus modBus = container.getEventBus();
        IEventBus bus = NeoForge.EVENT_BUS;

        // Mod-bus registration events (NeoForge fires these during client mod loading), for every NeoForge-style mod in the port
        List<RegisterGuiLayersEvent.Entry> allLayers = new java.util.ArrayList<>();
        List<net.minecraft.client.resources.model.sprite.AtlasManager.AtlasConfig> allAtlases = new java.util.ArrayList<>();
        java.util.Map<net.minecraft.world.level.material.Fluid, net.minecraft.client.renderer.block.FluidModel.Unbaked> allFluidModels = new java.util.LinkedHashMap<>();
        for (ModContainer mod : mekanism.fabric.bridge.ModBusLifecycle.mods()) {
            IEventBus bus0 = mod.getEventBus();
            bus0.post(new RegisterKeyMappingsEvent());
            bus0.post(new RegisterMenuScreensEvent());
            bus0.post(new EntityRenderersEvent.RegisterLayerDefinitions());
            bus0.post(new EntityRenderersEvent.RegisterRenderers());
            bus0.post(new RegisterParticleProvidersEvent());
            bus0.post(new RegisterClientExtensionsEvent());
            bus0.post(new RegisterItemDecorationsEvent());
            RegisterTextureAtlasesEvent atlases = new RegisterTextureAtlasesEvent();
            bus0.post(atlases);
            allAtlases.addAll(atlases.getAtlases());
            RegisterFluidModelsEvent fluidModels = new RegisterFluidModelsEvent();
            bus0.post(fluidModels);
            allFluidModels.putAll(fluidModels.getModels());
            RegisterGuiLayersEvent layers = new RegisterGuiLayersEvent();
            bus0.post(layers);
            allLayers.addAll(layers.getLayers());
            mod.getCustomExtension(IConfigScreenFactory.class).ifPresent(factory ->
                  ConfigScreenFactoryRegistry.INSTANCE.register(mod.getModId(), (modId, parent) -> factory.createScreen(mod, parent)));
        }
        registerGuiLayers(allLayers);
        allAtlases.forEach(net.fabricmc.fabric.api.client.rendering.v1.AtlasRegistry::register);
        mekanism.fabric.client.model.ModelBridge.init();
        registerFluidRenderHandlers(allFluidModels);
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
        net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent.EVENT.register((client, synchronizedRecipes) ->
              bus.post(new RecipesReceivedEvent(java.util.Set.of(), net.minecraft.world.item.crafting.RecipeMap.create(synchronizedRecipes.recipes()))));
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> blockEntity.onLoad());
        ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                blockEntity.onChunkUnloaded();
            }
        });
        ExtractItemDecorationsCallback.EVENT.register((graphics, font, stack, x, y) -> {
            if (stack.isEmpty()) {
                return;
            }
            List<mekanism.neoshim.neoforge.client.IItemDecorator> decorators =
                  mekanism.neoshim.neoforge.client.event.RegisterItemDecorationsEvent.DECORATORS.get(stack.getItem());
            if (decorators != null) {
                for (mekanism.neoshim.neoforge.client.IItemDecorator decorator : decorators) {
                    decorator.render(graphics, font, stack, x, y);
                }
            }
        });
        //NeoForge's RenderGuiLayerEvent.Pre for the crosshair (hidden while a radial menu shows its back button)
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (graphics, deltaTracker) -> {
            if (!NeoForge.EVENT_BUS.post(new RenderGuiLayerEvent.Pre(graphics, deltaTracker, VanillaGuiLayers.CROSSHAIR, original::extractRenderState)).isCanceled()) {
                original.extractRenderState(graphics, deltaTracker);
            }
        });
        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((worldContext, outlineState) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit && minecraft.level != null) {
                if (!outlineState.shape().isEmpty()) {
                    net.minecraft.core.BlockPos pos = outlineState.pos();
                    net.minecraft.world.level.block.state.BlockState state = minecraft.level.getBlockState(pos);
                    net.minecraft.world.phys.shapes.CollisionContext collisionContext = minecraft.player == null ?
                          net.minecraft.world.phys.shapes.CollisionContext.empty() : net.minecraft.world.phys.shapes.CollisionContext.of(minecraft.player);
                    ExtractBlockOutlineRenderStateEvent event = new ExtractBlockOutlineRenderStateEvent(worldContext.levelRenderer(), minecraft.level, pos, state, blockHit,
                          collisionContext, outlineState.isTranslucent(), outlineState.highContrast(), minecraft.gameRenderer.getMainCamera(),
                          worldContext.levelState());
                    NeoForge.EVENT_BUS.post(event);
                    if (!event.getCustomRenderers().isEmpty()) {
                        com.mojang.blaze3d.vertex.PoseStack poseStack = worldContext.poseStack() != null ? worldContext.poseStack() :
                              new com.mojang.blaze3d.vertex.PoseStack();
                        net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource = worldContext.bufferSource();
                        boolean handled = false;
                        for (mekanism.neoshim.neoforge.client.CustomBlockOutlineRenderer renderer : event.getCustomRenderers()) {
                            handled |= renderer.render(outlineState, bufferSource, poseStack, outlineState.isTranslucent(), worldContext.levelState());
                        }
                        if (handled) {
                            return false;
                        }
                    }
                }
                return !mekanism.neoshim.neoforge.client.ClientHooks.onDrawHighlight(worldContext.levelRenderer(), minecraft.gameRenderer.getMainCamera(), blockHit,
                      minecraft.getDeltaTracker(), worldContext.poseStack(), worldContext.bufferSource());
            }
            return true;
        });
        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT.register((client, screen, width, height) ->
              net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.afterExtract(screen).register((s, graphics, mouseX, mouseY, partialTick) ->
                    bus.post(new mekanism.neoshim.neoforge.client.event.ScreenEvent.Render.Post(s, graphics, mouseX, mouseY, partialTick))));
        LevelRenderEvents.AFTER_OPAQUE_TERRAIN.register(context -> postStage(context, RenderLevelStageEvent.AfterOpaqueBlocks::new));
        LevelRenderEvents.AFTER_SOLID_FEATURES.register(context -> postStage(context, RenderLevelStageEvent.AfterOpaqueFeatures::new));
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(context -> postStage(context, RenderLevelStageEvent.AfterTranslucentBlocks::new));
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(context -> postStage(context, RenderLevelStageEvent.AfterTranslucentFeatures::new));
        LevelRenderEvents.END_MAIN.register(context -> postStage(context, RenderLevelStageEvent.AfterLevel::new));
    }

    /** Called from MinecraftInitMixin right before the initial resource reload (GL context, texture manager and colors exist). */
    public static void onBeforeInitialReload(Minecraft minecraft, net.minecraft.server.packs.resources.ReloadableResourceManager resourceManager) {
        for (ModContainer container : mekanism.fabric.bridge.ModBusLifecycle.mods()) {
            IEventBus modBus = container.getEventBus();
            modBus.post(new RegisterColorHandlersEvent.BlockTintSources(minecraft.getBlockColors()));
            modBus.post(new RegisterColorHandlersEvent.ItemTintSources(ItemTintSources.ID_MAPPER));
            AddClientReloadListenersEvent reloadListeners = new AddClientReloadListenersEvent();
            modBus.post(reloadListeners);
            reloadListeners.getListeners().forEach(resourceManager::registerReloadListener);
        }
    }

    /** World rendering of fluids with the models registered by {@code RegisterFluidModelsEvent} (textures + tint). */
    private static void registerFluidRenderHandlers(
          java.util.Map<net.minecraft.world.level.material.Fluid, net.minecraft.client.renderer.block.FluidModel.Unbaked> models) {
        models.forEach(net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry::register);
    }

    /** Smoke-test diagnostic: counts Mekanism block states / items whose model baked to the missing model. */
    private static void logMissingModels(Minecraft client) {
        var blockStateModels = client.getModelManager().getBlockStateModelSet();
        var missing = blockStateModels.missingModel();
        java.util.List<String> missingBlocks = new java.util.ArrayList<>();
        int blockStates = 0;
        for (var block : net.minecraft.core.registries.BuiltInRegistries.BLOCK) {
            var id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);
            if (!id.getNamespace().startsWith("mekanism")) {
                continue;
            }
            for (var state : block.getStateDefinition().getPossibleStates()) {
                blockStates++;
                if (blockStateModels.get(state) == missing && state.getRenderShape() == net.minecraft.world.level.block.RenderShape.MODEL) {
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
            if (client.getModelManager().getItemModel(id) instanceof net.minecraft.client.renderer.item.MissingItemModel) {
                missingItems.add(id.toString());
            }
        }
        mekanism.common.Mekanism.logger.info("[Fabric port] Smoke test models: {}/{} block states missing, {}/{} items missing", missingBlocks.size(), blockStates,
              missingItems.size(), items);
        missingBlocks.stream().limit(40).forEach(s -> mekanism.common.Mekanism.logger.warn("[Fabric port] Missing block model: {}", s));
        missingItems.stream().limit(80).forEach(s -> mekanism.common.Mekanism.logger.warn("[Fabric port] Missing item model: {}", s));
    }

    /** Maps NeoForge's layer ids onto Fabric's HUD elements and inserts the registered layers around them. */
    private static void registerGuiLayers(List<RegisterGuiLayersEvent.Entry> entries) {
        for (RegisterGuiLayersEvent.Entry entry : entries) {
            HudElement element = entry.layer()::render;
            switch (entry.placement()) {
                case BELOW_ALL -> HudElementRegistry.addFirst(entry.id(), element);
                case ABOVE_ALL -> HudElementRegistry.addLast(entry.id(), element);
                case BELOW, ABOVE -> attachToAnchor(entry, element);
            }
        }
    }

    private static void attachToAnchor(RegisterGuiLayersEvent.Entry entry, HudElement element) {
        Identifier anchor = fabricAnchor(entry.anchor());
        try {
            if (entry.placement() == RegisterGuiLayersEvent.Placement.BELOW) {
                HudElementRegistry.attachElementBefore(anchor, entry.id(), element);
            } else {
                HudElementRegistry.attachElementAfter(anchor, entry.id(), element);
            }
        } catch (IllegalArgumentException e) {
            //Unknown anchor: keep the layer at the end of the stack
            HudElementRegistry.addLast(entry.id(), element);
        }
    }

    /** NeoForge's vanilla layer ids to Fabric's HUD element ids. */
    private static Identifier fabricAnchor(Identifier anchor) {
        if (!"minecraft".equals(anchor.getNamespace())) {
            return anchor;
        }
        return switch (anchor.getPath()) {
            case "camera_overlays" -> VanillaHudElements.MISC_OVERLAYS;
            case "crosshair" -> VanillaHudElements.CROSSHAIR;
            case "hotbar" -> VanillaHudElements.HOTBAR;
            case "armor_level" -> VanillaHudElements.ARMOR_BAR;
            case "player_health" -> VanillaHudElements.HEALTH_BAR;
            case "food_level" -> VanillaHudElements.FOOD_BAR;
            case "air_level" -> VanillaHudElements.AIR_BAR;
            case "vehicle_health" -> VanillaHudElements.MOUNT_HEALTH;
            case "experience_level" -> VanillaHudElements.EXPERIENCE_LEVEL;
            case "selected_item_name" -> VanillaHudElements.HELD_ITEM_TOOLTIP;
            case "spectator_tooltip" -> VanillaHudElements.SPECTATOR_TOOLTIP;
            case "effects" -> VanillaHudElements.MOB_EFFECTS;
            case "boss_overlay" -> VanillaHudElements.BOSS_BAR;
            case "sleep_overlay" -> VanillaHudElements.SLEEP;
            case "demo_overlay" -> VanillaHudElements.DEMO_TIMER;
            case "scoreboard_sidebar" -> VanillaHudElements.SCOREBOARD;
            case "overlay_message" -> VanillaHudElements.OVERLAY_MESSAGE;
            case "title" -> VanillaHudElements.TITLE_AND_SUBTITLE;
            case "subtitle_overlay" -> VanillaHudElements.SUBTITLES;
            case "chat" -> VanillaHudElements.CHAT;
            case "tab_list" -> VanillaHudElements.PLAYER_LIST;
            default -> anchor;
        };
    }

    @FunctionalInterface
    private interface StageFactory {

        RenderLevelStageEvent create(net.minecraft.client.renderer.LevelRenderer renderer, net.minecraft.client.renderer.state.level.LevelRenderState state,
              com.mojang.blaze3d.vertex.PoseStack poseStack, org.joml.Matrix4fc modelView,
              Iterable<? extends mekanism.neoshim.neoforge.client.IRenderableSection> sections);
    }

    private static void postStage(net.fabricmc.fabric.api.client.rendering.v1.level.AbstractLevelRenderContext context, StageFactory factory) {
        net.minecraft.client.renderer.state.level.CameraRenderState cameraState = context.levelState().cameraRenderState;
        com.mojang.blaze3d.vertex.PoseStack poseStack = context instanceof LevelRenderContext full && full.poseStack() != null ? full.poseStack() :
              new com.mojang.blaze3d.vertex.PoseStack();
        org.joml.Matrix4fc modelView = cameraState.viewRotationMatrix == null ? new org.joml.Matrix4f() : cameraState.viewRotationMatrix;
        NeoForge.EVENT_BUS.post(factory.create(context.levelRenderer(), context.levelState(), poseStack, modelView, List.of()));
    }

}
