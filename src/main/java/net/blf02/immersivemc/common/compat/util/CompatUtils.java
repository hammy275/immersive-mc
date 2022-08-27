package net.blf02.immersivemc.common.compat.util;

public class CompatUtils {

    public static Class<?> getClazz(String classPath) {
        try {
            return Class.forName(classPath);
        } catch (ClassNotFoundException e) {
            return NullClass.class;
        }
    }
}
