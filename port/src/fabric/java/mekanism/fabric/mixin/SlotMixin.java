package mekanism.fabric.mixin;

import com.mojang.datafixers.util.Pair;
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
    private Pair<Identifier, Identifier> mekanism$background;

    @Override
    public Slot setBackground(Identifier atlas, Identifier sprite) {
        this.mekanism$background = Pair.of(atlas, sprite);
        return (Slot) (Object) this;
    }

    @Inject(method = "getNoItemIcon", at = @At("HEAD"), cancellable = true)
    private void mekanism$getNoItemIcon(CallbackInfoReturnable<Pair<Identifier, Identifier>> cir) {
        if (mekanism$background != null) {
            cir.setReturnValue(mekanism$background);
        }
    }
}
