package mekanism.common.integration.gender;

import net.minecraft.world.level.ItemLike;
import mekanism.neoshim.neoforge.capabilities.RegisterCapabilitiesEvent;

/// FABRIC stub: Wildfire's Female Gender Mod is not available for Fabric 26.1 yet; the upstream integration is compiled out (NOTES.md phase 9).
public class MekanismGenderArmor {

    public static final MekanismGenderArmor OPEN_FRONT = new MekanismGenderArmor();
    public static final MekanismGenderArmor HIDES_BREASTS = new MekanismGenderArmor();
    public static final MekanismGenderArmor HAZMAT = new MekanismGenderArmor();

    private MekanismGenderArmor() {
    }

    public void register(RegisterCapabilitiesEvent event, ItemLike... items) {
    }
}
