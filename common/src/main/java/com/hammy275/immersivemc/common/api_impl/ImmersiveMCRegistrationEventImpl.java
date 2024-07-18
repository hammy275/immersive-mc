package com.hammy275.immersivemc.common.api_impl;

import com.hammy275.immersivemc.api.common.ImmersiveMCRegistrationEvent;

import java.util.Arrays;
import java.util.function.Consumer;

public class ImmersiveMCRegistrationEventImpl<T> implements ImmersiveMCRegistrationEvent<T> {

    private final Consumer<T> objectConsumer;

    public ImmersiveMCRegistrationEventImpl(Consumer<T> objectConsumer) {
        this.objectConsumer = objectConsumer;
    }

    @Override
    @SafeVarargs
    public final void register(T... objects) {
        Arrays.stream(objects).forEach(objectConsumer);
    }

    @Override
    public void register(Iterable<T> objects) {
        objects.forEach(objectConsumer);
    }
}
