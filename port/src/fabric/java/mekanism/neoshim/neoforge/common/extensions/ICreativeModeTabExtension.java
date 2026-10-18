package mekanism.neoshim.neoforge.common.extensions;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.IntSupplier;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Injected into {@link net.minecraft.world.item.CreativeModeTab}. NeoForge lets tabs override the label colour via a subclass ({@code withTabFactory}); vanilla
 * tabs cannot be subclassed on Fabric, so colours are registered per tab instead and CreativeModeInventoryScreenMixin draws the title with them.
 */
public interface ICreativeModeTabExtension {

    int DEFAULT_LABEL_COLOR = 0x404040;
    Map<CreativeModeTab, IntSupplier> LABEL_COLORS = java.util.Collections.synchronizedMap(new WeakHashMap<>());

    static void setLabelColor(CreativeModeTab tab, IntSupplier color) {
        LABEL_COLORS.put(tab, color);
    }

    default int getLabelColor() {
        IntSupplier color = LABEL_COLORS.get((CreativeModeTab) this);
        return color == null ? DEFAULT_LABEL_COLOR : color.getAsInt();
    }
}
