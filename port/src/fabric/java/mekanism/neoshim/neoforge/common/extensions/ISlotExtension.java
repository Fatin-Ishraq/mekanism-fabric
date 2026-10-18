package mekanism.neoshim.neoforge.common.extensions;

import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;

/** Injected into {@link Slot}; background storage is implemented by SlotMixin. */
public interface ISlotExtension {

    default Slot setBackground(Identifier atlas, Identifier sprite) {
        throw new IllegalStateException("Implemented by mixin");
    }

    default int getSlotIndex() {
        return ((Slot) this).getContainerSlot();
    }
}
