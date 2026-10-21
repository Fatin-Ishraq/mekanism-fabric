package mekanism.neoshim.neoforge.common.util.strategy;

import it.unimi.dsi.fastutil.Hash;

/** Identity-free (equals/hashCode based) strategy, used for data-component keyed maps. */
public class BasicStrategy implements Hash.Strategy<Object> {

    public static final Hash.Strategy<? super Object> BASIC = new BasicStrategy();

    @Override
    public int hashCode(Object o) {
        return o == null ? 0 : o.hashCode();
    }

    @Override
    public boolean equals(Object a, Object b) {
        return a == b || a != null && a.equals(b);
    }
}
