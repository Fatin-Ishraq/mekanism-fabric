package mekanism.fabric.bridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.InterModComms;
import mekanism.neoshim.fml.ModContainer;
import mekanism.neoshim.fml.event.config.ModConfigEvent;
import mekanism.neoshim.fml.event.lifecycle.FMLCommonSetupEvent;
import mekanism.neoshim.fml.event.lifecycle.InterModEnqueueEvent;
import mekanism.neoshim.fml.event.lifecycle.InterModProcessEvent;
import mekanism.neoshim.neoforge.attachment.AttachmentType;
import mekanism.neoshim.neoforge.capabilities.RegisterCapabilitiesEvent;
import mekanism.neoshim.neoforge.common.NeoForge;
import mekanism.neoshim.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import mekanism.neoshim.neoforge.event.AddServerReloadListenersEvent;
import mekanism.neoshim.neoforge.event.BuildCreativeModeTabContentsEvent;
import mekanism.neoshim.neoforge.event.ModifyDefaultComponentsEvent;
import mekanism.neoshim.neoforge.event.entity.EntityAttributeCreationEvent;
import mekanism.neoshim.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import mekanism.neoshim.neoforge.network.event.RegisterConfigurationTasksEvent;
import mekanism.neoshim.neoforge.network.event.RegisterPayloadHandlersEvent;
import mekanism.neoshim.neoforge.registries.DataPackRegistryEvent;
import mekanism.neoshim.neoforge.registries.NeoForgeRegistries;
import mekanism.neoshim.neoforge.registries.NewRegistryEvent;
import mekanism.neoshim.neoforge.registries.RegisterEvent;
import mekanism.neoshim.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.CreativeModeTab;

/** Fires NeoForge mod-bus lifecycle events for all NeoForge-style mods in this jar, in NeoForge's order (NOTES.md D2/D3). */
public final class ModBusLifecycle {

    private static final List<ModContainer> MODS = new ArrayList<>();

    private ModBusLifecycle() {
    }

    public static List<ModContainer> mods() {
        return MODS;
    }

    public static void addMod(ModContainer container) {
        MODS.add(container);
        //Bridge Forge Config API Port's config events onto the mod bus
        ModConfigEvents.loading(container.getModId()).register(config -> container.getEventBus().post(new ModConfigEvent.Loading(config)));
        ModConfigEvents.reloading(container.getModId()).register(config -> container.getEventBus().post(new ModConfigEvent.Reloading(config)));
        ModConfigEvents.unloading(container.getModId()).register(config -> container.getEventBus().post(new ModConfigEvent.Unloading(config)));
    }

    private static void postAll(Supplier<? extends Event> event) {
        for (ModContainer mod : MODS) {
            mod.getEventBus().post(event.get());
        }
    }

    private static void postAllShared(Event event) {
        for (ModContainer mod : MODS) {
            mod.getEventBus().post(event);
        }
    }

    public static void runCommonLifecycle() {
        // 1. Registries (force creation of the NeoForge-only registries first; only their Keys may have been touched so far)
        NeoForgeRegistries.FLUID_TYPES.key();
        mekanism.fabric.compat.ConditionBridge.registerBuiltins();
        postAll(NewRegistryEvent::new);
        postAll(DataPackRegistryEvent.NewRegistry::new);
        //NeoForge fires a few registries first (components/attributes are needed by item and entity constructors), then modded registries,
        // then the rest in vanilla order (blocks before items)
        List<ResourceKey<? extends Registry<?>>> keys = new ArrayList<>(List.of(Registries.DATA_COMPONENT_TYPE, Registries.ATTRIBUTE, Registries.SOUND_EVENT,
              Registries.MOB_EFFECT, Registries.PARTICLE_TYPE));
        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            if (!registry.key().location().getNamespace().equals("minecraft") && !keys.contains(registry.key())) {
                keys.add(registry.key());
            }
        }
        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            if (!keys.contains(registry.key())) {
                keys.add(registry.key());
            }
        }
        for (ResourceKey<? extends Registry<?>> key : keys) {
            Registry<?> registry = BuiltInRegistries.REGISTRY.getValue(key.location());
            for (ModContainer mod : MODS) {
                mod.getEventBus().post(new RegisterEvent(key, registry));
            }
        }
        bindNeoForgeOnlyRegistries();
        mekanism.fabric.compat.ConditionBridge.registerAll();

        // 2. Registration-time events
        RegisterCapabilitiesEvent capabilities = new RegisterCapabilitiesEvent();
        postAllShared(capabilities);
        postAll(RegisterPayloadHandlersEvent::new);
        postAll(EntityAttributeCreationEvent::new);
        postAll(RegisterSpawnPlacementsEvent::new);
        postAll(RegisterDataMapTypesEvent::new);
        postAll(RegisterTicketControllersEvent::new);
        hookDefaultComponents();
        hookCreativeTabs();
        hookConfigurationTasks();

        // 3. Setup + IMC
        for (ModContainer mod : MODS) {
            mod.getEventBus().post(new FMLCommonSetupEvent(mod));
        }
        for (ModContainer mod : MODS) {
            InterModComms.setCurrentSender(mod.getModId());
            mod.getEventBus().post(new InterModEnqueueEvent(mod));
            InterModComms.setCurrentSender(null);
        }
        for (ModContainer mod : MODS) {
            mod.getEventBus().post(new InterModProcessEvent(mod));
        }

        // 4. Server data reload listeners (collected once; NeoForge collects per reload)
        int index = 0;
        for (ModContainer mod : MODS) {
            AddServerReloadListenersEvent reloadEvent = new AddServerReloadListenersEvent();
            mod.getEventBus().post(reloadEvent);
            for (PreparableReloadListener listener : reloadEvent.getListeners()) {
                Identifier id = Identifier.fromNamespaceAndPath("mekanism_fabric", "reload_listener_" + index++);
                ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return id;
                    }

                    @Override
                    public java.util.Collection<Identifier> getFabricDependencies() {
                        return List.of(ResourceReloadListenerKeys.RECIPES);
                    }

                    @Override
                    public java.util.concurrent.CompletableFuture<Void> reload(PreparableReloadListener.SharedState state,
                          java.util.concurrent.Executor backgroundExecutor, PreparationBarrier barrier, java.util.concurrent.Executor gameExecutor) {
                        return listener.reload(state, backgroundExecutor, barrier, gameExecutor);
                    }

                    @Override
                    public String getName() {
                        return listener.getName();
                    }
                });
            }
        }
    }

    /** Registries that only exist for NeoForge semantics need their entries pushed into the vanilla/Fabric systems. */
    private static void bindNeoForgeOnlyRegistries() {
        for (Map.Entry<ResourceKey<AttachmentType<?>>, AttachmentType<?>> entry : NeoForgeRegistries.ATTACHMENT_TYPES.entrySet()) {
            entry.getValue().bind(entry.getKey().location());
        }
        for (EntityDataSerializer<?> serializer : NeoForgeRegistries.ENTITY_DATA_SERIALIZERS) {
            EntityDataSerializers.registerSerializer(serializer);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void hookDefaultComponents() {
        ModifyDefaultComponentsEvent event = new ModifyDefaultComponentsEvent();
        DefaultItemComponentEvents.MODIFY.register(context -> {
            postAllShared(event);
            for (ModifyDefaultComponentsEvent.Modification modification : event.getModifications()) {
                context.modify(modification.filter(), modification.initializer()::run);
            }
        });
    }

    private static void hookCreativeTabs() {
        CreativeModeTabEvents.MODIFY_OUTPUT_ALL.register((group, output) -> {
            ResourceKey<CreativeModeTab> key = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(group).orElse(null);
            if (key != null) {
                postAllShared(new BuildCreativeModeTabContentsEvent(group, key, output));
            }
        });
    }

    private static void hookConfigurationTasks() {
        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> postAllShared(new RegisterConfigurationTasksEvent(handler, handler::addTask)));
    }
}
