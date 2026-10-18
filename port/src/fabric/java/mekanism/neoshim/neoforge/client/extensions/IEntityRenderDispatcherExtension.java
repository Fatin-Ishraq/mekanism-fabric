package mekanism.neoshim.neoforge.client.extensions;

import java.util.Map;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.player.Player;

/** Injected into {@link EntityRenderDispatcher}. */
public interface IEntityRenderDispatcherExtension {

    default Map<PlayerSkin.Model, EntityRenderer<? extends Player>> getSkinMap() {
        return ((EntityRenderDispatcher) this).playerRenderers;
    }
}
