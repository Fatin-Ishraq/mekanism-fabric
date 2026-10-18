package mekanism.neoshim.neoforge.client.extensions;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;

/** Injected into {@link ItemRenderer}. */
public interface IItemRendererExtension {

    default BlockEntityWithoutLevelRenderer getBlockEntityRenderer() {
        return ((ItemRenderer) this).blockEntityRenderer;
    }
}
