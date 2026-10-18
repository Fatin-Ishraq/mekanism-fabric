package mekanism.neoshim.neoforge.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EffectCure {

    private static final Map<String, EffectCure> CURES = new ConcurrentHashMap<>();

    public static EffectCure get(String name) {
        return CURES.computeIfAbsent(name, EffectCure::new);
    }

    private final String name;

    private EffectCure(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
}
