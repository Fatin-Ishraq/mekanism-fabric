package mekanism.neoshim.neoforge.common.extensions;

import mekanism.neoshim.neoforge.common.util.ValueIOSerializable;
import net.minecraft.world.level.storage.ValueOutput;

/** Injected into {@link ValueOutput}. NeoForge adds nested-object helpers on top of the vanilla child API. */
public interface IValueOutputExtension {

    default void putChild(String key, ValueIOSerializable child) {
        child.serialize(((ValueOutput) this).child(key));
    }
}
