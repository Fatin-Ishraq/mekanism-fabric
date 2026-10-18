package mekanism.client.model;

import mekanism.common.Mekanism;
import net.minecraft.data.PackOutput;
import mekanism.neoshim.neoforge.common.data.ExistingFileHelper;

public class MekanismBlockModelProvider extends BaseBlockModelProvider {

    public MekanismBlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Mekanism.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

    }
}