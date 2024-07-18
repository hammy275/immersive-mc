package com.hammy275.immersivemc.common.api_impl;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.common.ImmersiveMCRegistration;
import com.hammy275.immersivemc.api.common.ImmersiveMCRegistrationEvent;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ImmersiveMCRegistrationImpl implements ImmersiveMCRegistration {

    public static final ImmersiveMCRegistration INSTANCE = new ImmersiveMCRegistrationImpl();
    private static final Set<Consumer<ImmersiveMCRegistrationEvent<ImmersiveHandler<?>>>> HANDLERS = new HashSet<>();
    private static boolean didRegistration = false;

    public static void doImmersiveRegistration(Consumer<ImmersiveHandler<?>> immersiveHandlerConsumer) {
        if (didRegistration) {
            throw new IllegalStateException("Already did ImmersiveHandler registration!");
        }
        ImmersiveMCRegistrationEvent<ImmersiveHandler<?>> event = new ImmersiveMCRegistrationEventImpl<>(immersiveHandlerConsumer);
        for (Consumer<ImmersiveMCRegistrationEvent<ImmersiveHandler<?>>> handler : HANDLERS) {
            handler.accept(event);
        }
        ImmersiveMC.handlerIMCRegistrationHandler.accept(event); // Register ImmersiveMC's handlers last
        didRegistration = true;
    }

    @Override
    public void addImmersiveHandlerRegistrationHandler(Consumer<ImmersiveMCRegistrationEvent<ImmersiveHandler<?>>> registrationHandler) throws IllegalStateException {
        synchronized (this) {
            if (didRegistration) {
                throw new IllegalStateException("Can't add a registration handler for ImmersiveHandlers after Immersives have been registered.");
            }
            HANDLERS.add(registrationHandler);
        }
    }
}
