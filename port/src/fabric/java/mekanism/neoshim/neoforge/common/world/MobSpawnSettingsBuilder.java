package mekanism.neoshim.neoforge.common.world;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private final Map<MobCategory, WeightedList.Builder<MobSpawnSettings.SpawnerData>> spawners = new EnumMap<>(MobCategory.class);
    private final Map<MobCategory, Integer> initialSizes = new EnumMap<>(MobCategory.class);
    private final Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> costs = new HashMap<>();
    private final Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> addedCosts = new HashMap<>();
    private float probability = 0.1F;

    public MobSpawnSettingsBuilder() {
    }

    /** Seeded with existing settings; only later additions are reported by {@link #getAddedSpawners()} / {@link #getAddedCosts()}. */
    public MobSpawnSettingsBuilder(MobSpawnSettings existing) {
        this.probability = existing.getCreatureProbability();
        for (MobCategory category : MobCategory.values()) {
            List<Weighted<MobSpawnSettings.SpawnerData>> mobs = existing.getMobs(category).unwrap();
            WeightedList.Builder<MobSpawnSettings.SpawnerData> builder = getSpawner(category);
            mobs.forEach(builder::add);
            initialSizes.put(category, mobs.size());
        }
        for (EntityType<?> type : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE) {
            MobSpawnSettings.MobSpawnCost cost = existing.getMobSpawnCost(type);
            if (cost != null) {
                costs.put(type, cost);
            }
        }
    }

    /** Spawners added since construction, per category. */
    public Map<MobCategory, List<Weighted<MobSpawnSettings.SpawnerData>>> getAddedSpawners() {
        Map<MobCategory, List<Weighted<MobSpawnSettings.SpawnerData>>> added = new EnumMap<>(MobCategory.class);
        spawners.forEach((category, builder) -> {
            List<Weighted<MobSpawnSettings.SpawnerData>> list = builder.getList();
            int initial = initialSizes.getOrDefault(category, 0);
            if (list.size() > initial) {
                added.put(category, list.subList(initial, list.size()));
            }
        });
        return added;
    }

    public Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> getAddedCosts() {
        return addedCosts;
    }

    public Set<MobCategory> getSpawnerTypes() {
        return spawners.keySet();
    }

    public WeightedList.Builder<MobSpawnSettings.SpawnerData> getSpawner(MobCategory category) {
        return spawners.computeIfAbsent(category, c -> WeightedList.builder());
    }

    public Set<EntityType<?>> getEntityTypes() {
        return costs.keySet();
    }

    @Nullable
    public MobSpawnSettings.MobSpawnCost getCost(EntityType<?> type) {
        return costs.get(type);
    }

    public float getProbability() {
        return probability;
    }

    @Override
    public MobSpawnSettings.Builder addSpawn(MobCategory category, int weight, MobSpawnSettings.SpawnerData data) {
        getSpawner(category).add(data, weight);
        return super.addSpawn(category, weight, data);
    }

    @Override
    public MobSpawnSettings.Builder addMobCharge(EntityType<?> type, double charge, double energyBudget) {
        costs.put(type, new MobSpawnSettings.MobSpawnCost(energyBudget, charge));
        addedCosts.put(type, new MobSpawnSettings.MobSpawnCost(energyBudget, charge));
        return super.addMobCharge(type, charge, energyBudget);
    }

    @Override
    public MobSpawnSettings.Builder creatureGenerationProbability(float probability) {
        this.probability = probability;
        return super.creatureGenerationProbability(probability);
    }
}
