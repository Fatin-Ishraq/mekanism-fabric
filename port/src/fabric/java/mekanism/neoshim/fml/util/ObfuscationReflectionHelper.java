package mekanism.neoshim.fml.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Field/method lookup. NOTE: in production Fabric runs on intermediary names, so Mojang names passed here only resolve in dev. Mekanism's
 * uses (FieldReflectionHelper) are audited in NOTES.md "Hook status".
 */
public class ObfuscationReflectionHelper {

    public static Field findField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new UnableToFindFieldException(e);
        }
    }

    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new UnableToFindMethodException(e);
        }
    }

    public static class UnableToFindFieldException extends RuntimeException {

        public UnableToFindFieldException(Exception e) {
            super(e);
        }
    }

    public static class UnableToFindMethodException extends RuntimeException {

        public UnableToFindMethodException(Throwable failed) {
            super(failed);
        }
    }
}
