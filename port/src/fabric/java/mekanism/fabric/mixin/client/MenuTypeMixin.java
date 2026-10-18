package mekanism.fabric.mixin.client;

import io.netty.buffer.Unpooled;
import mekanism.fabric.menu.ExtendedMenus;
import mekanism.neoshim.neoforge.network.IContainerFactory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Client half of {@link ExtendedMenus}: feeds pending extra opening data to IContainerFactory-based menu types (NeoForge semantics). */
@Mixin(MenuType.class)
public abstract class MenuTypeMixin<T extends AbstractContainerMenu> {

    @Shadow
    @Final
    private MenuType.MenuSupplier<T> constructor;

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private void mekanism$createWithData(int containerId, Inventory inventory, CallbackInfoReturnable<T> cir) {
        if (constructor instanceof IContainerFactory<T> factory && inventory.player.level().isClientSide) {
            byte[] data = ExtendedMenus.takePendingClientData();
            if (data != null) {
                RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), inventory.player.registryAccess());
                cir.setReturnValue(factory.create(containerId, inventory, buf));
            }
        }
    }
}
