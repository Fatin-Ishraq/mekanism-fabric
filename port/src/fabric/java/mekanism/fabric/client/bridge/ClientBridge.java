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
import net.minecraft.resources.ResourceLocation;
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

        // Mod-bus registration events (NeoForge fires these during client mod loading)
        modBus.post(new RegisterKeyMappingsEvent());
        modBus.post(new RegisterMenuScreensEvent());
        modBus.post(new EntityRenderersEvent.RegisterLayerDefinitions());
        modBus.post(new EntityRenderersEvent.RegisterRenderers());
        modBus.post(new RegisterParticleProvidersEvent());
        modBus.post(new RegisterClientExtensionsEvent());
        modBus.post(new RegisterItemDecorationsEvent());
        modBus.post(new ModelEvent.RegisterGeometryLoaders());
        RegisterGuiLayersEvent layers = new RegisterGuiLayersEvent();
        modBus.post(layers);
        guiLayers = List.copyOf(layers.getLayers());

        container.getCustomExtension(IConfigScreenFactory.class).ifPresent(factory ->
              ConfigScreenFactoryRegistry.INSTANCE.register(container.getModId(), (modId, parent) -> factory.createScreen(container, parent)));
        modBus.post(new FMLClientSetupEvent(container));

        // Dev-only automated boot check (gradlew runSmokeClient): quit once the title screen is reached, no interaction
        if (Boolean.getBoolean("mekanism_fabric.smokeTest")) {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (client.getOverlay() == null && client.screen != null) {
                    mekanism.common.Mekanism.logger.info("[Fabric port] Smoke test: resource loading finished, showing {}; stopping client",
                          client.screen.getClass().getSimpleName());
                    client.stop();
                }
            });
        }

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(mekanism.fabric.datamaps.DataMapLoader.SyncPayload.TYPE,
              (payload, context) -> mekanism.fabric.datamaps.DataMapLoader.applySync(payload.data(), context.client().getConnection().registryAccess()));

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

    private static void postStage(RenderLevelStageEvent.Stage stage, WorldRenderContext context) {
        com.mojang.blaze3d.vertex.PoseStack poseStack = context.matrixStack() != null ? context.matrixStack() : new com.mojang.blaze3d.vertex.PoseStack();
        NeoForge.EVENT_BUS.post(new RenderLevelStageEvent(stage, context.worldRenderer(), poseStack, context.positionMatrix(), context.projectionMatrix(),
              context.worldRenderer().getTicks(), context.tickCounter(), context.camera(), context.frustum()));
    }

}
