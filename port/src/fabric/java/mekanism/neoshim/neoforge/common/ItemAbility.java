package mekanism.neoshim.neoforge.common;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ItemAbility {

    private static final Map<String, ItemAbility> ABILITIES = new ConcurrentHashMap<>();
    public static final Codec<ItemAbility> CODEC = Codec.STRING.xmap(ItemAbility::get, ItemAbility::name);

    public static Collection<ItemAbility> getActions() {
        return Collections.unmodifiableCollection(ABILITIES.values());
    }

    public static ItemAbility get(String name) {
        return ABILITIES.computeIfAbsent(name, ItemAbility::new);
    }

    private final String name;

    private ItemAbility(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "ItemAbility[" + name + "]";
    }
}
