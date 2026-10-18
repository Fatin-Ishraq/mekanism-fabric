package mekanism.fabric.mixin.client;

import mekanism.fabric.bridge.ModBusLifecycle;
import mekanism.neoshim.fml.ModContainer;
import mekanism.neoshim.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's EntityRenderersEvent.AddLayers, fired after entity renderers are (re)created. */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Shadow
    @Final
    private ItemRenderer itemRenderer;
    @Shadow
    @Final
    private BlockRenderDispatcher blockRenderDispatcher;
    @Shadow
    @Final
    private ItemInHandRenderer itemInHandRenderer;
    @Shadow
    @Final
    private Font font;
    @Shadow
    @Final
    private EntityModelSet entityModels;

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void mekanism$addLayers(ResourceManager resourceManager, CallbackInfo ci) {
        EntityRenderDispatcher self = (EntityRenderDispatcher) (Object) this;
        EntityRendererProvider.Context context = new EntityRendererProvider.Context(self, itemRenderer, blockRenderDispatcher, itemInHandRenderer, resourceManager,
              entityModels, font);
        for (ModContainer mod : ModBusLifecycle.mods()) {
            mod.getEventBus().post(new EntityRenderersEvent.AddLayers(self, context));
        }
    }
}
