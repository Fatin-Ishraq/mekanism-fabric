package mekanism.neoshim.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import mekanism.neoshim.api.distmarker.Dist;

/** Marker only on Fabric; mod entrypoints are declared in fabric.mod.json and constructed by the {@code mekanism.fabric} bootstrap. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {

    String value();

    Dist[] dist() default {Dist.CLIENT, Dist.DEDICATED_SERVER};
}
