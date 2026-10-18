package mekanism.neoshim.neoforge.common.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

/** Biome modifier type (datapack driven on NeoForge). On Fabric, BiomeModifierBridge replays the modifiers shipped in mod jars via BiomeModifications. */
public interface BiomeModifier {

    void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder);

    MapCodec<? extends BiomeModifier> codec();

    enum Phase {
        BEFORE_EVERYTHING,
        ADD,
        REMOVE,
        MODIFY,
        AFTER_EVERYTHING
    }
}
