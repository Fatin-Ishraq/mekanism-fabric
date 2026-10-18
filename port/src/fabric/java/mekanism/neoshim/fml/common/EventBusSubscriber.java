package mekanism.neoshim.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import mekanism.neoshim.api.distmarker.Dist;

/** No classpath scanning on Fabric: annotated classes are registered explicitly by the bootstrap. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventBusSubscriber {

    Dist[] value() default {Dist.CLIENT, Dist.DEDICATED_SERVER};

    String modid() default "";

    Bus bus() default Bus.GAME;

    enum Bus {
        GAME,
        MOD
    }
}
