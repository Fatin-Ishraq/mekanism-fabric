package mekanism.fabric.mixin;

import mekanism.neoshim.neoforge.common.extensions.IItemPropertiesExtension;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.Properties.class)
public abstract class ItemPropertiesMixin implements IItemPropertiesExtension {

    @Unique
    private boolean mekanism$noRepair;

    @Override
    public Item.Properties setNoRepair() {
        this.mekanism$noRepair = true;
        return (Item.Properties) (Object) this;
    }
}
