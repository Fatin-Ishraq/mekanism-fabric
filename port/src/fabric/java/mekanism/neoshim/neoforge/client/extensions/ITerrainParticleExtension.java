package mekanism.neoshim.neoforge.client.extensions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link TerrainParticle}. */
public interface ITerrainParticleExtension {

    default TerrainParticle updateSprite(BlockState state, @Nullable BlockPos pos) {
        TerrainParticle self = (TerrainParticle) this;
        if (pos != null) {
            self.sprite = Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();//FABRIC: sprite field is access widened; vanilla has no setSprite
        }
        return self;
    }
}
