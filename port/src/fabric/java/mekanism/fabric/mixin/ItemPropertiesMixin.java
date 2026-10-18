package mekanism.fabric.mixin;

import mekanism.neoshim.neoforge.common.extensions.IItemPropertiesExtension;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.Properties.class)
public abstract class ItemPropertiesMixin implements IItemPropertiesExtension {

    @Unique
    private boolean mekanism$noCombineRepair;

    @Override
    public Item.Properties setNoCombineRepair() {
        this.mekanism$noCombineRepair = true;
        return (Item.Properties) (Object) this;
    }

    @Override
    public boolean mekanism$noCombineRepair() {
        return mekanism$noCombineRepair;
    }
}
