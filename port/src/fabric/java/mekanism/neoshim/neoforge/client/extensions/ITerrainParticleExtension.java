package mekanism.neoshim.neoforge.client.extensions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Injected into {@link TerrainParticle}. */
public interface ITerrainParticleExtension {

    default TerrainParticle updateSprite(BlockState state, BlockPos pos) {
        TerrainParticle self = (TerrainParticle) this;
        if (pos != null) {
            self.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(state));
        }
        return self;
    }
}
