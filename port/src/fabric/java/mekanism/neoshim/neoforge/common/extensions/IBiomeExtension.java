package mekanism.neoshim.neoforge.common.extensions;

import net.minecraft.world.level.biome.Biome;

/** Injected into {@link Biome}. Biome modifiers do not change climate on Fabric, so this is the vanilla climate. */
public interface IBiomeExtension {

    default Biome.ClimateSettings getModifiedClimateSettings() {
        return ((Biome) (Object) this).climateSettings;
    }
}
