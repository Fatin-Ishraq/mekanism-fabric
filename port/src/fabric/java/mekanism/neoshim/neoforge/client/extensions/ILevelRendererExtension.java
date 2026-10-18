package mekanism.neoshim.neoforge.client.extensions;

import net.minecraft.client.renderer.LevelRenderer;

/** Injected into {@link LevelRenderer}. */
public interface ILevelRendererExtension {

    default int getTicks() {
        return ((LevelRenderer) this).ticks;
    }
}
