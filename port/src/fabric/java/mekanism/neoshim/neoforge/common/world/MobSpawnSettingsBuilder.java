package mekanism.neoshim.neoforge.common.world;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.jetbrains.annotations.Nullable;

/**
 * Readable mob spawn settings builder (NeoForge biome modifiers). BiomeModifierBridge seeds it with a biome's current spawns and replays what a modifier added
 * through Fabric's BiomeModifications.
 */
public class MobSpawnSettingsBuilder extends MobSpawnSettings.Builder {

    private final Map<MobCategory, List<MobSpawnSettings.SpawnerData>> spawners = new EnumMap<>(MobCategory.class);
    private final Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> costs = new HashMap<>();
    private final Map<MobCategory, List<MobSpawnSettings.SpawnerData>> addedSpawners = new EnumMap<>(MobCategory.class);
    private final Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> addedCosts = new HashMap<>();

    public MobSpawnSettingsBuilder() {
    }

    /** Seeded with existing settings; only later additions are reported by {@link #getAddedSpawners()} / {@link #getAddedCosts()}. */
    public MobSpawnSettingsBuilder(MobSpawnSettings existing) {
        for (MobCategory category : MobCategory.values()) {
            getSpawner(category).addAll(existing.getMobs(category).unwrap());
        }
        for (EntityType<?> type : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE) {
            MobSpawnSettings.MobSpawnCost cost = existing.getMobSpawnCost(type);
            if (cost != null) {
                costs.put(type, cost);
            }
        }
    }

    public Map<MobCategory, List<MobSpawnSettings.SpawnerData>> getAddedSpawners() {
        return addedSpawners;
    }

    public Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> getAddedCosts() {
        return addedCosts;
    }

    public List<MobSpawnSettings.SpawnerData> getSpawner(MobCategory category) {
        return spawners.computeIfAbsent(category, c -> new ArrayList<>());
    }

    @Nullable
    public MobSpawnSettings.MobSpawnCost getCost(EntityType<?> type) {
        return costs.get(type);
    }

    @Override
    public MobSpawnSettings.Builder addSpawn(MobCategory category, MobSpawnSettings.SpawnerData data) {
        getSpawner(category).add(data);
        addedSpawners.computeIfAbsent(category, c -> new ArrayList<>()).add(data);
        return super.addSpawn(category, data);
    }

    @Override
    public MobSpawnSettings.Builder addMobCharge(EntityType<?> type, double charge, double energyBudget) {
        costs.put(type, new MobSpawnSettings.MobSpawnCost(energyBudget, charge));
        addedCosts.put(type, new MobSpawnSettings.MobSpawnCost(energyBudget, charge));
        return super.addMobCharge(type, charge, energyBudget);
    }
}
