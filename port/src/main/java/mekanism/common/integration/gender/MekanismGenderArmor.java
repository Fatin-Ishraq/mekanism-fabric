package mekanism.common.integration.gender;

import com.wildfire.api.IGenderArmor;
import mekanism.common.Mekanism;
import net.minecraft.world.level.ItemLike;
import mekanism.neoshim.neoforge.capabilities.RegisterCapabilitiesEvent;

public record MekanismGenderArmor(boolean coversBreasts, boolean alwaysHidesBreasts, float physicsResistance, float tightness, boolean armorStandsCopySettings) implements IGenderArmor {

    //FABRIC: the Fabric build of Wildfire's Gender Mod registers armor through WildfireAPI#addGenderArmor instead of an item capability
    public static final MekanismGenderArmor OPEN_FRONT = new MekanismGenderArmor(false, false, 0, 0, false);
    public static final MekanismGenderArmor HIDES_BREASTS = new MekanismGenderArmor(true, true, 0, 0, false);
    public static final MekanismGenderArmor HAZMAT = new MekanismGenderArmor(0.5F, 0.25F, false);

    public MekanismGenderArmor(float physicsResistance) {
        this(physicsResistance, 0);
    }

    public MekanismGenderArmor(float physicsResistance, float tightness) {
        this(physicsResistance, tightness, true);
    }

    public MekanismGenderArmor(float physicsResistance, float tightness, boolean armorStandsCopySettings) {
        this(true, false, physicsResistance, tightness, armorStandsCopySettings);
    }

    public MekanismGenderArmor {
        if (physicsResistance < 0 || physicsResistance > 1) {
            throw new IllegalArgumentException("Physics resistance must be between zero and one inclusive.");
        } else if (tightness < 0 || tightness > 1) {
            throw new IllegalArgumentException("Armor tightness must be between zero and one inclusive.");
        }
    }

    public void register(RegisterCapabilitiesEvent event, ItemLike... items) {
        for (ItemLike item : items) {
            com.wildfire.api.WildfireAPI.addGenderArmor(item.asItem(), this);//FABRIC: see above
        }
    }
}