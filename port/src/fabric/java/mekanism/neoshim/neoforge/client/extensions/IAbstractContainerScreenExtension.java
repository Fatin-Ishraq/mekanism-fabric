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

    @Nullable
    default Slot getHoveredSlot() {
        return ((AbstractContainerScreen<?>) this).hoveredSlot;
    }

    default int getLeftPos() {
        return ((AbstractContainerScreen<?>) this).leftPos;
    }

    default int getTopPos() {
        return ((AbstractContainerScreen<?>) this).topPos;
    }

    default int getImageWidth() {
        return ((AbstractContainerScreen<?>) this).imageWidth;
    }

    default int getImageHeight() {
        return ((AbstractContainerScreen<?>) this).imageHeight;
    }
}
