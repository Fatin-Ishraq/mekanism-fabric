package mekanism.neoshim.neoforge.common.extensions;

import mekanism.neoshim.neoforge.capabilities.EntityCapability;
import mekanism.neoshim.neoforge.fluids.FluidType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link Entity}. */
public interface IEntityExtension {

    private Entity self() {
        return (Entity) this;
    }

    @Nullable
    default <T, C> T getCapability(EntityCapability<T, C> capability, C context) {
        return capability.getCapability(self(), context);
    }

    @Nullable
    default <T> T getCapability(EntityCapability<T, Void> capability) {
        return capability.getCapability(self(), null);
    }

    default boolean isFakePlayer() {
        return self() instanceof net.fabricmc.fabric.api.entity.FakePlayer;
    }

    default FluidType getMaxHeightFluidType() {
        if (self().isInLava()) {
            return FluidType.VanillaTypes.LAVA;
        } else if (self().isInWater()) {
            return FluidType.VanillaTypes.WATER;
        }
        return FluidType.VanillaTypes.EMPTY;
    }

    default void onRemovedFromLevel() {
    }

    default void onAddedToLevel() {
    }

    @Nullable
    default ItemStack getPickedResult(HitResult target) {
        return self().getPickResult();
    }
}
