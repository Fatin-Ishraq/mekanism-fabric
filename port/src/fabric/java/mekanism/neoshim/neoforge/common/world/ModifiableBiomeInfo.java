package mekanism.neoshim.neoforge.common.world;

import net.minecraft.world.level.biome.BiomeGenerationSettings;

public class ModifiableBiomeInfo {

    public record BiomeInfo(BiomeGenerationSettings generationSettings) {

        public static class Builder {

            private final BiomeGenerationSettings.PlainBuilder generationSettings;

            public Builder(BiomeGenerationSettings.PlainBuilder generationSettings) {
                this.generationSettings = generationSettings;
            }

            public BiomeGenerationSettings.PlainBuilder getGenerationSettings() {
                return generationSettings;
            }

            public mekanism.neoshim.neoforge.common.world.MobSpawnSettingsBuilder getMobSpawnSettings() {
                return mobSpawnSettings;
            }

            private final mekanism.neoshim.neoforge.common.world.MobSpawnSettingsBuilder mobSpawnSettings = new mekanism.neoshim.neoforge.common.world.MobSpawnSettingsBuilder();
        }
    }
}
