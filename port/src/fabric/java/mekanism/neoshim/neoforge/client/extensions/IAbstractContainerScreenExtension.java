package mekanism.neoshim.neoforge.client.extensions;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

/** Injected into {@link AbstractContainerScreen}. */
public interface IAbstractContainerScreenExtension {

    @Nullable
    default Slot getSlotUnderMouse() {
        return ((AbstractContainerScreen<?>) this).hoveredSlot;
    }
}
