package mekanism.fabric.mixin.client;

import java.util.Map;
import java.util.function.Supplier;
import mekanism.fabric.bridge.ModBusLifecycle;
import mekanism.neoshim.fml.ModContainer;
import mekanism.neoshim.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;
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
    private BlockModelResolver blockModelResolver;
    @Shadow
    @Final
    private ItemModelResolver itemModelResolver;
    @Shadow
    @Final
    private MapRenderer mapRenderer;
    @Shadow
    @Final
    private AtlasManager atlasManager;
    @Shadow
    @Final
    private Font font;
    @Shadow
    @Final
    private Supplier<EntityModelSet> entityModels;
    @Shadow
    @Final
    private EquipmentAssetManager equipmentAssets;
    @Shadow
    @Final
    private PlayerSkinRenderCache playerSkinRenderCache;
    @Shadow
    private Map<EntityType<?>, EntityRenderer<?, ?>> renderers;
    @Shadow
    private Map<PlayerModelType, AvatarRenderer<AbstractClientPlayer>> playerRenderers;
    @Shadow
    private Map<PlayerModelType, AvatarRenderer<ClientMannequin>> mannequinRenderers;

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void mekanism$addLayers(ResourceManager resourceManager, CallbackInfo ci) {
        EntityRenderDispatcher self = (EntityRenderDispatcher) (Object) this;
        EntityRendererProvider.Context context = new EntityRendererProvider.Context(self, blockModelResolver, itemModelResolver, mapRenderer, resourceManager,
              entityModels.get(), equipmentAssets, atlasManager, font, playerSkinRenderCache);
        for (ModContainer mod : ModBusLifecycle.mods()) {
            mod.getEventBus().post(new EntityRenderersEvent.AddLayers(renderers, playerRenderers, mannequinRenderers, context));
        }
    }
}
