package mekanism.neoshim.neoforge.util;

import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;

/** 26.1 vanilla dropped the {@code ContextMap.EMPTY} shortcut NeoForge mods used for property-free bakes and recipe display lookups. */
public final class ContextMaps {

    public static final ContextMap EMPTY = new ContextMap.Builder().create(new ContextKeySet.Builder().build());

    private ContextMaps() {
    }
}
