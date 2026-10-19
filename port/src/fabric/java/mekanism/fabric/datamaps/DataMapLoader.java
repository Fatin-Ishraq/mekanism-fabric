package mekanism.fabric.datamaps;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import mekanism.common.Mekanism;
import mekanism.neoshim.neoforge.common.NeoForge;
import mekanism.neoshim.neoforge.registries.datamaps.DataMapType;
import mekanism.neoshim.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge data maps on Fabric (NOTES.md "Hook status"): {@code data/<ns>/data_maps/<registry>/<name>.json} files are parsed on datapack
 * reload, resolved (including {@code #tag} keys) once tags are bound, published through {@link DataMapsUpdatedEvent}, and synced to remote
 * clients for {@link DataMapType#networkCodec() synced} maps. The {@code neoforge:furnace_fuels} map is applied to Fabric's FuelRegistry.
 */
public final class DataMapLoader {

    private static final Map<DataMapType<?, ?>, Map<ResourceKey<?>, Object>> VALUES = new ConcurrentHashMap<>();
    private static final Map<Identifier, DataMapType<?, ?>> TYPES = new ConcurrentHashMap<>();
    /** Raw entries from the last reload, waiting for tags to be bound. */
    private static Map<DataMapType<?, ?>, List<RawEntry>> pending = Map.of();
    private static final Identifier FURNACE_FUELS = Identifier.fromNamespaceAndPath("neoforge", "furnace_fuels");
    private static final Map<Item, Integer> appliedFuels = new HashMap<>();

    private record RawEntry(String key, JsonElement value, boolean remove) {
    }

    private DataMapLoader() {
    }

    public static void register(DataMapType<?, ?> type) {
        VALUES.putIfAbsent(type, new ConcurrentHashMap<>());
        TYPES.put(type.id(), type);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <R, T> T getData(DataMapType<R, T> type, Holder<R> holder) {
        Map<ResourceKey<?>, Object> values = VALUES.get(type);
        if (values == null) {
            return null;
        }
        return holder.unwrapKey().map(key -> (T) values.get(key)).orElse(null);
    }

    public static Map<DataMapType<?, ?>, Map<ResourceKey<?>, Object>> values() {
        return VALUES;
    }

    /** Wires the reload listener, tag-bound resolution and client sync. Called once from the common bootstrap. */
    public static void init() {
        register(DataMapType.builder(FURNACE_FUELS, Registries.ITEM, FurnaceFuel.CODEC).build());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Identifier.fromNamespaceAndPath("mekanism_fabric", "data_maps");
            }

            @Override
            public java.util.Collection<Identifier> getFabricDependencies() {
                return List.of(ResourceReloadListenerKeys.TAGS);
            }

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, ProfilerFiller prepProfiler, ProfilerFiller applyProfiler,
                  Executor backgroundExecutor, Executor gameExecutor) {
                return CompletableFuture.supplyAsync(() -> load(manager), backgroundExecutor)
                      .thenCompose(barrier::wait)
                      .thenAcceptAsync(loaded -> pending = loaded, gameExecutor);
            }
        });
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if (!client) {
                resolve(registries);
            }
        });
        PayloadTypeRegistry.playS2C().register(SyncPayload.TYPE, SyncPayload.STREAM_CODEC);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            if (!player.server.isSingleplayerOwner(player.getGameProfile())) {
                ServerPlayNetworking.send(player, createSyncPayload(player.registryAccess()));
            }
        });
    }

    private static Map<DataMapType<?, ?>, List<RawEntry>> load(ResourceManager manager) {
        Map<DataMapType<?, ?>, List<RawEntry>> result = new HashMap<>();
        for (DataMapType<?, ?> type : TYPES.values()) {
            Identifier registry = type.registryKey().identifier();
            String registryPath = registry.getNamespace().equals("minecraft") ? registry.getPath() : registry.getNamespace() + "/" + registry.getPath();
            Identifier file = Identifier.fromNamespaceAndPath(type.id().getNamespace(), "data_maps/" + registryPath + "/" + type.id().getPath() + ".json");
            List<RawEntry> entries = new ArrayList<>();
            for (Resource resource : manager.getResourceStack(file)) {
                try (Reader reader = resource.openAsReader()) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    if (json.has("replace") && json.get("replace").getAsBoolean()) {
                        entries.clear();
                    }
                    if (json.has("values")) {
                        for (Map.Entry<String, JsonElement> value : json.getAsJsonObject("values").entrySet()) {
                            JsonElement element = value.getValue();
                            if (element.isJsonObject() && element.getAsJsonObject().has("replace") && element.getAsJsonObject().has("value")) {
                                element = element.getAsJsonObject().get("value");
                            }
                            entries.add(new RawEntry(value.getKey(), element, false));
                        }
                    }
                    if (json.has("remove")) {
                        for (JsonElement removed : json.getAsJsonArray("remove")) {
                            entries.add(new RawEntry(removed.getAsString(), null, true));
                        }
                    }
                } catch (Exception e) {
                    Mekanism.logger.error("Failed to load data map {} from {}", type.id(), resource.sourcePackId(), e);
                }
            }
            result.put(type, entries);
        }
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void resolve(RegistryAccess registries) {
        RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
        for (Map.Entry<DataMapType<?, ?>, List<RawEntry>> entry : pending.entrySet()) {
            DataMapType type = entry.getKey();
            Registry registry = (Registry) registries.registry(type.registryKey()).orElse(null);
            if (registry == null) {
                continue;
            }
            Map<ResourceKey<?>, Object> resolved = new LinkedHashMap<>();
            for (RawEntry raw : entry.getValue()) {
                List<ResourceKey<?>> keys = new ArrayList<>();
                if (raw.key().startsWith("#")) {
                    TagKey tag = TagKey.create(type.registryKey(), Identifier.parse(raw.key().substring(1)));
                    registry.getTag(tag).ifPresent(set -> ((Iterable<Holder>) set).forEach(h -> h.unwrapKey().ifPresent(k -> keys.add((ResourceKey<?>) k))));
                } else {
                    keys.add(ResourceKey.create(type.registryKey(), Identifier.parse(raw.key())));
                }
                if (raw.remove()) {
                    keys.forEach(resolved::remove);
                    continue;
                }
                DataResult<?> value = ((Codec) type.codec()).parse(ops, raw.value());
                value.resultOrPartial(error -> Mekanism.logger.error("Invalid value for data map {} entry {}: {}", type.id(), raw.key(), error))
                      .ifPresent(v -> keys.forEach(k -> resolved.put(k, v)));
            }
            VALUES.put(type, new ConcurrentHashMap<>(resolved));
            fireUpdated(registries, registry, DataMapsUpdatedEvent.UpdateCause.SERVER_RELOAD);
        }
        applyFurnaceFuels();
    }

    private static void fireUpdated(RegistryAccess registries, Registry<?> registry, DataMapsUpdatedEvent.UpdateCause cause) {
        NeoForge.EVENT_BUS.post(new DataMapsUpdatedEvent(registries, registry, cause));
    }

    private static void applyFurnaceFuels() {
        DataMapType<?, ?> fuels = TYPES.get(FURNACE_FUELS);
        for (Item item : appliedFuels.keySet()) {
            FuelRegistry.INSTANCE.remove(item);
        }
        appliedFuels.clear();
        Map<ResourceKey<?>, Object> values = VALUES.getOrDefault(fuels, Map.of());
        for (Map.Entry<ResourceKey<?>, Object> entry : values.entrySet()) {
            net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(entry.getKey().identifier()).ifPresent(item -> {
                int burnTime = ((FurnaceFuel) entry.getValue()).burnTime();
                FuelRegistry.INSTANCE.add(item, burnTime);
                appliedFuels.put(item, burnTime);
            });
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static SyncPayload createSyncPayload(HolderLookup.Provider registries) {
        CompoundTag root = new CompoundTag();
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        for (Map.Entry<DataMapType<?, ?>, Map<ResourceKey<?>, Object>> entry : VALUES.entrySet()) {
            DataMapType type = entry.getKey();
            if (type.networkCodec() == null) {
                continue;
            }
            ListTag list = new ListTag();
            for (Map.Entry<ResourceKey<?>, Object> value : entry.getValue().entrySet()) {
                ((Codec) type.networkCodec()).encodeStart(ops, value.getValue()).result().ifPresent(encoded -> {
                    CompoundTag pair = new CompoundTag();
                    pair.putString("key", value.getKey().identifier().toString());
                    pair.put("value", (Tag) encoded);
                    list.add(pair);
                });
            }
            root.put(type.id().toString(), list);
        }
        return new SyncPayload(root);
    }

    /** Applies a sync payload on the client (remote servers only). */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void applySync(CompoundTag root, RegistryAccess registries) {
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        for (String typeId : root.getAllKeys()) {
            DataMapType type = TYPES.get(Identifier.parse(typeId));
            if (type == null || type.networkCodec() == null) {
                continue;
            }
            Map<ResourceKey<?>, Object> values = new ConcurrentHashMap<>();
            for (Tag element : root.getList(typeId, Tag.TAG_COMPOUND)) {
                CompoundTag pair = (CompoundTag) element;
                ResourceKey<?> key = ResourceKey.create(type.registryKey(), Identifier.parse(pair.getString("key")));
                ((Codec) type.networkCodec()).parse(ops, pair.get("value")).result().ifPresent(v -> values.put(key, v));
            }
            VALUES.put(type, values);
            registries.registry(type.registryKey()).ifPresent(registry -> fireUpdated(registries, (Registry<?>) registry, DataMapsUpdatedEvent.UpdateCause.CLIENT_SYNC));
        }
    }

    public record FurnaceFuel(int burnTime) {

        public static final Codec<FurnaceFuel> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.create(instance -> instance.group(
              com.mojang.serialization.Codec.INT.fieldOf("burn_time").forGetter(FurnaceFuel::burnTime)
        ).apply(instance, FurnaceFuel::new));
    }

    public record SyncPayload(CompoundTag data) implements CustomPacketPayload {

        public static final Type<SyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("mekanism_fabric", "data_maps"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncPayload> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.<RegistryFriendlyByteBuf>cast()
              .map(SyncPayload::new, SyncPayload::data);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
