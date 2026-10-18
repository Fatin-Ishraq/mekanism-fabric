package mekanism.fabric.bridge;

import mekanism.fabric.FabricSides;
import mekanism.neoshim.bus.api.IEventBus;
import mekanism.neoshim.neoforge.capabilities.CapabilityInvalidation;
import mekanism.neoshim.neoforge.common.NeoForge;
import mekanism.neoshim.neoforge.event.OnDatapackSyncEvent;
import mekanism.neoshim.neoforge.event.RegisterCommandsEvent;
import mekanism.neoshim.neoforge.event.TagsUpdatedEvent;
import mekanism.neoshim.neoforge.event.entity.EntityJoinLevelEvent;
import mekanism.neoshim.neoforge.event.entity.living.LivingDeathEvent;
import mekanism.neoshim.neoforge.event.entity.player.PlayerEvent;
import mekanism.neoshim.neoforge.event.entity.player.PlayerInteractEvent;
import mekanism.neoshim.neoforge.event.level.BlockEvent;
import mekanism.neoshim.neoforge.event.level.ChunkEvent;
import mekanism.neoshim.neoforge.event.level.LevelEvent;
import mekanism.neoshim.neoforge.event.server.ServerStartedEvent;
import mekanism.neoshim.neoforge.event.server.ServerStartingEvent;
import mekanism.neoshim.neoforge.event.server.ServerStoppedEvent;
import mekanism.neoshim.neoforge.event.server.ServerStoppingEvent;
import mekanism.neoshim.neoforge.event.tick.LevelTickEvent;
import mekanism.neoshim.neoforge.event.tick.ServerTickEvent;
import mekanism.neoshim.neoforge.server.ServerLifecycleHooks;
import mekanism.neoshim.neoforge.server.permission.events.PermissionGatherEvent;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Bridges Fabric API callbacks onto the shim game bus ({@link NeoForge#EVENT_BUS}). Each mapping is listed in NOTES.md "Hook status". */
public final class GameEventBridge {

    private GameEventBridge() {
    }

    public static void init() {
        IEventBus bus = NeoForge.EVENT_BUS;
        // Server lifecycle
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ServerLifecycleHooks.setCurrentServer(server);
            FabricSides.markServerThread(Thread.currentThread());
            bus.post(new PermissionGatherEvent.Nodes());
            bus.post(new ServerStartingEvent(server));
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> bus.post(new ServerStartedEvent(server)));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> bus.post(new ServerStoppingEvent(server)));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            bus.post(new ServerStoppedEvent(server));
            ServerLifecycleHooks.setCurrentServer(null);
        });
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> bus.post(new OnDatapackSyncEvent(player.server.getPlayerList(), player)));
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> bus.post(new TagsUpdatedEvent(registries, client, false)));
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> bus.post(new RegisterCommandsEvent(dispatcher, selection, context)));

        // Ticks
        ServerTickEvents.START_SERVER_TICK.register(server -> bus.post(new ServerTickEvent.Pre(() -> true, server)));
        ServerTickEvents.END_SERVER_TICK.register(server -> bus.post(new ServerTickEvent.Post(() -> true, server)));
        ServerTickEvents.START_WORLD_TICK.register(level -> bus.post(new LevelTickEvent.Pre(() -> true, level)));
        ServerTickEvents.END_WORLD_TICK.register(level -> bus.post(new LevelTickEvent.Post(() -> true, level)));

        // Levels / chunks / block entities
        ServerWorldEvents.LOAD.register((server, level) -> bus.post(new LevelEvent.Load(level)));
        ServerWorldEvents.UNLOAD.register((server, level) -> {
            bus.post(new LevelEvent.Unload(level));
            CapabilityInvalidation.clear(level);
        });
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk) -> {
            bus.post(new ChunkEvent.Load(chunk, false));
            CapabilityInvalidation.invalidate(level, chunk.getPos());
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                blockEntity.onChunkUnloaded();
            }
            bus.post(new ChunkEvent.Unload(chunk));
            CapabilityInvalidation.invalidate(level, chunk.getPos());
        });
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> {
            blockEntity.onLoad();
            CapabilityInvalidation.invalidate(level, blockEntity.getBlockPos());
        });
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, level) -> CapabilityInvalidation.invalidate(level, blockEntity.getBlockPos()));

        // Entities / players
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> bus.post(new EntityJoinLevelEvent(entity, level, false)));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> entity.onRemovedFromLevel());
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> bus.post(new PlayerEvent.PlayerLoggedInEvent(handler.player)));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> bus.post(new PlayerEvent.PlayerLoggedOutEvent(handler.player)));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> bus.post(new PlayerEvent.PlayerRespawnEvent(newPlayer, alive)));
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> bus.post(new PlayerEvent.Clone(newPlayer, oldPlayer, !alive)));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) ->
              bus.post(new PlayerEvent.PlayerChangedDimensionEvent(player, origin.dimension(), destination.dimension())));
        EntityTrackingEvents.START_TRACKING.register((target, player) -> bus.post(new PlayerEvent.StartTracking(player, target)));
        EntityTrackingEvents.STOP_TRACKING.register((target, player) -> bus.post(new PlayerEvent.StopTracking(player, target)));
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> !bus.post(new LivingDeathEvent(entity, source)).isCanceled());

        // Interaction
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) ->
              !bus.post(new BlockEvent.BreakEvent(level, pos, state, player)).isCanceled());
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            PlayerInteractEvent.RightClickBlock event = bus.post(new PlayerInteractEvent.RightClickBlock(player, hand, hitResult.getBlockPos(), hitResult));
            if (event.isCanceled()) {
                return event.getCancellationResult() == InteractionResult.PASS ? InteractionResult.FAIL : event.getCancellationResult();
            }
            return InteractionResult.PASS;
        });
    }
}
