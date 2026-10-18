package mekanism.neoshim.neoforge.client.extensions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Injected into {@link net.minecraft.client.renderer.entity.layers.ElytraLayer}; ElytraLayerMixin consults these. */
public interface IElytraLayerExtension<T extends LivingEntity> {

    ResourceLocation WINGS_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/elytra.png");

    default boolean shouldRender(ItemStack stack, T entity) {
        return stack.is(Items.ELYTRA);
    }

    default ResourceLocation getElytraTexture(ItemStack stack, T entity) {
        return WINGS_LOCATION;
    }
}
