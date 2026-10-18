package mekanism.neoshim.neoforge.common.world;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;

public class StructureSettingsBuilder {

    private final Map<MobCategory, StructureSpawnOverrideBuilder> spawnOverrides = new HashMap<>();

    public StructureSpawnOverrideBuilder getOrAddSpawnOverrides(MobCategory category) {
        return spawnOverrides.computeIfAbsent(category, c -> new StructureSpawnOverrideBuilder());
    }

    @org.jetbrains.annotations.Nullable
    public StructureSpawnOverrideBuilder getSpawnOverrides(MobCategory category) {
        return spawnOverrides.get(category);
    }

    public void removeSpawnOverrides(MobCategory category) {
        spawnOverrides.remove(category);
    }

    public static class StructureSpawnOverrideBuilder {

        private StructureSpawnOverride.BoundingBoxType boundingBox = StructureSpawnOverride.BoundingBoxType.PIECE;
        private final java.util.List<MobSpawnSettings.SpawnerData> spawns = new java.util.ArrayList<>();

        public StructureSpawnOverrideBuilder setBoundingBox(StructureSpawnOverride.BoundingBoxType boundingBox) {
            this.boundingBox = boundingBox;
            return this;
        }

        public StructureSpawnOverride.BoundingBoxType getBoundingBox() {
            return boundingBox;
        }

        public StructureSpawnOverrideBuilder addSpawn(MobSpawnSettings.SpawnerData spawn) {
            spawns.add(spawn);
            return this;
        }

        public java.util.List<MobSpawnSettings.SpawnerData> getSpawns() {
            return spawns;
        }
    }
}
