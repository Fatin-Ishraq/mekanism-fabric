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

/** Readable mob spawn settings builder (NeoForge biome modifiers). Biome modifiers are not executed on Fabric; see BiomeModifierBridge. */
public class MobSpawnSettingsBuilder extends MobSpawnSettings.Builder {

    private final Map<MobCategory, List<MobSpawnSettings.SpawnerData>> spawners = new EnumMap<>(MobCategory.class);
    private final Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> costs = new HashMap<>();

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
        return super.addSpawn(category, data);
    }

    @Override
    public MobSpawnSettings.Builder addMobCharge(EntityType<?> type, double charge, double energyBudget) {
        costs.put(type, new MobSpawnSettings.MobSpawnCost(energyBudget, charge));
        return super.addMobCharge(type, charge, energyBudget);
    }
}
