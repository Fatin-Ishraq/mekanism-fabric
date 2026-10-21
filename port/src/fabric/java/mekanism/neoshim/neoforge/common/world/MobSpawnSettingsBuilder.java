package mekanism.neoshim.neoforge.common.world;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.jetbrains.annotations.Nullable;

/**
 * Readable mob spawn settings builder (NeoForge biome modifiers). BiomeModifierBridge seeds it with a biome's current spawns and replays what a modifier added
 * through Fabric's BiomeModifications.
 */
public class MobSpawnSettingsBuilder extends MobSpawnSettings.Builder {

    private final Map<MobCategory, TrackingBuilder> spawners = new EnumMap<>(MobCategory.class);
    private final Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> costs = new HashMap<>();
    private final Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> addedCosts = new HashMap<>();

    public MobSpawnSettingsBuilder() {
    }

    /** Seeded with existing settings; only later additions are reported by {@link #getAddedSpawners()} / {@link #getAddedCosts()}. */
    public MobSpawnSettingsBuilder(MobSpawnSettings existing) {
        for (MobCategory category : MobCategory.values()) {
            TrackingBuilder builder = getTrackingSpawner(category);
            for (Weighted<MobSpawnSettings.SpawnerData> weighted : existing.getMobs(category).unwrap()) {
                builder.add(weighted.value(), weighted.weight());
            }
            builder.clearAdded();
        }
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            MobSpawnSettings.MobSpawnCost cost = existing.getMobSpawnCost(type);
            if (cost != null) {
                costs.put(type, cost);
            }
        }
    }

    public Map<MobCategory, List<Weighted<MobSpawnSettings.SpawnerData>>> getAddedSpawners() {
        Map<MobCategory, List<Weighted<MobSpawnSettings.SpawnerData>>> added = new EnumMap<>(MobCategory.class);
        spawners.forEach((category, builder) -> {
            List<Weighted<MobSpawnSettings.SpawnerData>> entries = builder.getAdded();
            if (!entries.isEmpty()) {
                added.put(category, List.copyOf(entries));
            }
        });
        return added;
    }

    public Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> getAddedCosts() {
        return addedCosts;
    }

    public WeightedList.Builder<MobSpawnSettings.SpawnerData> getSpawner(MobCategory category) {
        return getTrackingSpawner(category);
    }

    private TrackingBuilder getTrackingSpawner(MobCategory category) {
        return spawners.computeIfAbsent(category, c -> new TrackingBuilder());
    }

    @Nullable
    public MobSpawnSettings.MobSpawnCost getCost(EntityType<?> type) {
        return costs.get(type);
    }

    @Override
    public MobSpawnSettings.Builder addMobCharge(EntityType<?> type, double charge, double energyBudget) {
        MobSpawnSettings.MobSpawnCost cost = new MobSpawnSettings.MobSpawnCost(energyBudget, charge);
        costs.put(type, cost);
        addedCosts.put(type, cost);
        return super.addMobCharge(type, charge, energyBudget);
    }

    /// Records entries added after seeding so the bridge can replay them into Fabric's modification context.
    private static class TrackingBuilder extends WeightedList.Builder<MobSpawnSettings.SpawnerData> {

        private final List<Weighted<MobSpawnSettings.SpawnerData>> added = new ArrayList<>();

        @Override
        public WeightedList.Builder<MobSpawnSettings.SpawnerData> add(MobSpawnSettings.SpawnerData data) {
            added.add(new Weighted<>(data, 1));
            return super.add(data);
        }

        @Override
        public WeightedList.Builder<MobSpawnSettings.SpawnerData> add(MobSpawnSettings.SpawnerData data, int weight) {
            added.add(new Weighted<>(data, weight));
            return super.add(data, weight);
        }

        private List<Weighted<MobSpawnSettings.SpawnerData>> getAdded() {
            return added;
        }

        private void clearAdded() {
            added.clear();
        }
    }
}
