package com.hammy275.immersivemc.common.util;

import java.util.function.Supplier;

/**
 * A supplier that caches the value after the first retrieval.
 * @param <T> Return value type.
 */
public class MemoizedSupplier<T> implements Supplier<T> {

    private final Supplier<T> valueSupplier;
    private T value;
    private boolean calculated = false;

    public MemoizedSupplier(Supplier<T> valueSupplier) {
        this.valueSupplier = valueSupplier;
    }

    public MemoizedSupplier(T value) {
        this.valueSupplier = null;
        this.calculated = true;
        this.value = value;
    }

    @Override
    public T get() {
        if (!calculated) {
            value = valueSupplier.get();
            calculated = true;
        }
        return value;
    }
}
