package mekanism.neoshim.neoforge.capabilities;

import mekanism.fabric.compat.CapabilityBridge;
import mekanism.neoshim.bus.api.Event;
import mekanism.neoshim.fml.event.IModBusEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Registers capability providers into the Fabric lookups backing each capability, plus the interop bridges (NOTES.md D4). */
public class RegisterCapabilitiesEvent extends Event implements IModBusEvent {

    public void setProxyable(BlockCapability<?, ?> capability) {
        capability.setProxyable();
    }

    public <T, C> void registerBlock(BlockCapability<T, C> capability, IBlockCapabilityProvider<T, C> provider, Block... blocks) {
        capability.lookup().registerForBlocks(provider::getCapability, blocks);
        CapabilityBridge.onBlockRegistered(capability, blocks);
    }

    @SuppressWarnings("unchecked")
    public <T, C, BE extends BlockEntity> void registerBlockEntity(BlockCapability<T, C> capability, BlockEntityType<BE> blockEntityType,
          ICapabilityProvider<? super BE, C, T> provider) {
        capability.lookup().registerForBlockEntity((be, context) -> provider.getCapability(be, context), blockEntityType);
        CapabilityBridge.onBlockEntityRegistered(capability, blockEntityType);
    }

    public <T, C extends @org.jetbrains.annotations.Nullable Object> void registerItem(ItemCapability<T, C> capability, ICapabilityProvider<ItemStack, C, T> provider,
          ItemLike... items) {
        capability.lookup().registerForItems(provider::getCapability, items);
        CapabilityBridge.onItemRegistered(capability, items);
    }

    @SuppressWarnings("unchecked")
    public <T, C, E extends Entity> void registerEntity(EntityCapability<T, C> capability, EntityType<E> entityType, ICapabilityProvider<? super E, C, T> provider) {
        capability.lookup().registerForTypes((entity, context) -> provider.getCapability((E) entity, context), entityType);
        CapabilityBridge.onEntityRegistered(capability, entityType);
    }

    public boolean isBlockRegistered(BlockCapability<?, ?> capability, Block block) {
        return capability.lookup().getProvider(block) != null;
    }

    public boolean isItemRegistered(ItemCapability<?, ?> capability, net.minecraft.world.item.Item item) {
        return capability.lookup().getProvider(item) != null;
    }

    public boolean isEntityRegistered(EntityCapability<?, ?> capability, EntityType<?> entityType) {
        return false;
    }
}
