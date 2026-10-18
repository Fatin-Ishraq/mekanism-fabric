package mekanism.neoshim.neoforge.common.extensions;

import java.util.Optional;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;

/** Injected into {@link BucketPickup}. */
public interface IBucketPickupExtension {

    default Optional<SoundEvent> getPickupSound(BlockState state) {
        return ((BucketPickup) this).getPickupSound();
    }
}
