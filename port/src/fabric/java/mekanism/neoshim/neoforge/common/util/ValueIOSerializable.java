package mekanism.neoshim.neoforge.common.util;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Something saved through the value input/output API. */
public interface ValueIOSerializable {

    void serialize(ValueOutput output);

    void deserialize(ValueInput input);
}
