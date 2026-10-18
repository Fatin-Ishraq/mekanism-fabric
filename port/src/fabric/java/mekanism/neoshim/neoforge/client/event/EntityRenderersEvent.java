package mekanism.neoshim.neoforge.client.event;

import java.util.Set;
import java.util.function.Supplier;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public abstract class EntityRenderersEvent extends Event implements IModBusEvent {

    public static class RegisterLayerDefinitions extends EntityRenderersEvent {

        public void registerLayerDefinition(ModelLayerLocation layerLocation, Supplier<LayerDefinition> supplier) {
            EntityModelLayerRegistry.registerModelLayer(layerLocation, supplier::get);
        }
    }

    public static class RegisterRenderers extends EntityRenderersEvent {

        public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> entityType, EntityRendererProvider<T> entityRendererProvider) {
            EntityRendererRegistry.register(entityType, entityRendererProvider);
        }

        public <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<? extends T> blockEntityType,
              BlockEntityRendererProvider<T> blockEntityRendererProvider) {
            BlockEntityRenderers.register(blockEntityType, blockEntityRendererProvider);
        }
    }

    /** Fired from Fabric's LivingEntityFeatureRendererRegistrationCallback-backed bridge after renderers are created. */
    public static class AddLayers extends EntityRenderersEvent {

        private final EntityRenderDispatcher dispatcher;
        private final EntityRendererProvider.Context context;

        public AddLayers(EntityRenderDispatcher dispatcher, EntityRendererProvider.Context context) {
            this.dispatcher = dispatcher;
            this.context = context;
        }

        public Set<EntityType<?>> getEntityTypes() {
            return dispatcher.renderers.keySet();
        }

        public Set<PlayerSkin.Model> getSkins() {
            return dispatcher.getSkinMap().keySet();
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <E extends Player, R extends EntityRenderer<E>> R getSkin(PlayerSkin.Model skinModel) {
            return (R) dispatcher.getSkinMap().get(skinModel);
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <E extends Entity, R extends EntityRenderer<E>> R getRenderer(EntityType<? extends E> entityType) {
            return (R) dispatcher.renderers.get(entityType);
        }

        public EntityModelSet getEntityModels() {
            return context.getModelSet();
        }

        public EntityRendererProvider.Context getContext() {
            return context;
        }
    }
}
