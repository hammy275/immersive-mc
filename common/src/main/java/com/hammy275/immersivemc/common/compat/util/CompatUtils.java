package com.hammy275.immersivemc.common.compat.util;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class CompatUtils {

    public static Class<?> getClazz(String classPath) {
        try {
            return Class.forName(classPath);
        } catch (ClassNotFoundException e) {
            return NullClass.class;
        }
    }

    @Nullable
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
