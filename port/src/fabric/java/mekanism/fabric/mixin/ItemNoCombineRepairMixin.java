package mekanism.fabric.mixin;

import mekanism.neoshim.neoforge.common.extensions.IItemPropertiesExtension;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Remembers items whose properties were marked {@link IItemPropertiesExtension#setNoCombineRepair()}. */
@Mixin(Item.class)
public abstract class ItemNoCombineRepairMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void mekanism$recordNoCombineRepair(Item.Properties properties, CallbackInfo ci) {
        if (properties.mekanism$noCombineRepair()) {
            IItemPropertiesExtension.NO_COMBINE_REPAIR.add((Item) (Object) this);
        }
    }
}
