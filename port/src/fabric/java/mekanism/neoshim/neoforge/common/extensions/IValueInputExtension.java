package mekanism.neoshim.neoforge.common.extensions;

import mekanism.neoshim.neoforge.common.util.ValueIOSerializable;
import net.minecraft.world.level.storage.ValueInput;

/** Injected into {@link ValueInput}. NeoForge adds nested-object helpers on top of the vanilla child API. */
public interface IValueInputExtension {

    default void readChild(String key, ValueIOSerializable child) {
        ((ValueInput) this).child(key).ifPresent(child::deserialize);
    }
}
