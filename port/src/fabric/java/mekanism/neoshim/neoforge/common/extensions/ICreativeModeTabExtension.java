package mekanism.neoshim.neoforge.common.extensions;

/** Injected into {@link net.minecraft.world.item.CreativeModeTab}. */
public interface ICreativeModeTabExtension {

    default int getLabelColor() {
        return 0x404040;
    }
}
