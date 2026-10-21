package mekanism.fabric.mixin;

import mekanism.neoshim.neoforge.common.extensions.ISlotExtension;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin implements ISlotExtension {

    @Unique
    private Identifier mekanism$background;

    @Override
    public Slot setBackground(Identifier sprite) {
        this.mekanism$background = sprite;
        return (Slot) (Object) this;
    }

    @Inject(method = "getNoItemIcon", at = @At("HEAD"), cancellable = true)
    private void mekanism$getNoItemIcon(CallbackInfoReturnable<Identifier> cir) {
        if (mekanism$background != null) {
            cir.setReturnValue(mekanism$background);
        }
    }
}
