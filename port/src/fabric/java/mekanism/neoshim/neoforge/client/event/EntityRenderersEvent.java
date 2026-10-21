package mekanism.neoshim.neoforge.client.event;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public abstract class EntityRenderersEvent extends Event implements IModBusEvent {

    public static class RegisterLayerDefinitions extends EntityRenderersEvent {

        public void registerLayerDefinition(ModelLayerLocation layerLocation, Supplier<LayerDefinition> supplier) {
            ModelLayerRegistry.registerModelLayer(layerLocation, supplier::get);
        }
    }

    public static class RegisterRenderers extends EntityRenderersEvent {

        public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> entityType, EntityRendererProvider<T> entityRendererProvider) {
            EntityRendererRegistry.register(entityType, entityRendererProvider);
        }

        public <T extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderer(BlockEntityType<? extends T> blockEntityType,
              BlockEntityRendererProvider<T, S> blockEntityRendererProvider) {
            @SuppressWarnings("unchecked")
            BlockEntityType<T> type = (BlockEntityType<T>) blockEntityType;
            BlockEntityRendererRegistry.register(type, blockEntityRendererProvider);
        }
    }

    /** Fired from the EntityRenderDispatcher reload hook after renderers are (re)created. */
    public static class AddLayers extends EntityRenderersEvent {

        private final Map<EntityType<?>, EntityRenderer<?, ?>> renderers;
        private final Map<PlayerModelType, AvatarRenderer<AbstractClientPlayer>> playerRenderers;
        private final Map<PlayerModelType, AvatarRenderer<ClientMannequin>> mannequinRenderers;
        private final EntityRendererProvider.Context context;

        public AddLayers(Map<EntityType<?>, EntityRenderer<?, ?>> renderers, Map<PlayerModelType, AvatarRenderer<AbstractClientPlayer>> playerRenderers,
              Map<PlayerModelType, AvatarRenderer<ClientMannequin>> mannequinRenderers, EntityRendererProvider.Context context) {
            this.renderers = renderers;
            this.playerRenderers = playerRenderers;
            this.mannequinRenderers = mannequinRenderers;
            this.context = context;
        }

        public Set<EntityType<?>> getEntityTypes() {
            return renderers.keySet();
        }

        public Set<PlayerModelType> getSkins() {
            return playerRenderers.keySet();
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <R extends AvatarRenderer<AbstractClientPlayer>> R getPlayerRenderer(PlayerModelType type) {
            return (R) playerRenderers.get(type);
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <R extends AvatarRenderer<ClientMannequin>> R getMannequinRenderer(PlayerModelType type) {
            return (R) mannequinRenderers.get(type);
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <T extends Entity, R extends EntityRenderer<T, ?>> R getRenderer(EntityType<? extends T> entityType) {
            return (R) renderers.get(entityType);
        }

        public EntityModelSet getEntityModels() {
            return context.getModelSet();
        }

        public EntityRendererProvider.Context getContext() {
            return context;
        }
    }
}
