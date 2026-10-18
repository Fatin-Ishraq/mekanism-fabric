package mekanism.neoshim.neoforge.common.extensions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

/** Injected into {@link Slot}; background storage is implemented by SlotMixin. */
public interface ISlotExtension {

    default Slot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
        throw new IllegalStateException("Implemented by mixin");
    }

    default int getSlotIndex() {
        return ((Slot) this).getContainerSlot();
    }
}
