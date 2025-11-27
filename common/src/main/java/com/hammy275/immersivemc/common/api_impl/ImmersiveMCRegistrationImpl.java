package com.hammy275.immersivemc.common.api_impl;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.common.ImmersiveMCRegistration;
import com.hammy275.immersivemc.api.common.ImmersiveMCRegistrationEvent;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.petting.PettingHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ImmersiveMCRegistrationImpl implements ImmersiveMCRegistration {

    public static final ImmersiveMCRegistration INSTANCE = new ImmersiveMCRegistrationImpl();

    private static final Set<Consumer<ImmersiveMCRegistrationEvent<ImmersiveHandler<?>>>> HANDLERS = new HashSet<>();
    private static boolean didRegistration = false;

    private static final Set<Consumer<ImmersiveMCRegistrationEvent<PettingHandler<?>>>> PETTING_HANDLERS = new HashSet<>();
    private static boolean didPettingRegistration = false;

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

    public static void doPettingHandlerRegistration(Consumer<PettingHandler<?>> pettingHandlerConsumer) {
        if (didPettingRegistration) {
            throw new IllegalStateException("Already did PettingHandler registration!");
        }
        ImmersiveMCRegistrationEvent<PettingHandler<?>> event = new ImmersiveMCRegistrationEventImpl<>(pettingHandlerConsumer);
        for (Consumer<ImmersiveMCRegistrationEvent<PettingHandler<?>>> handler : PETTING_HANDLERS) {
            handler.accept(event);
        }
        ImmersiveMC.pettingIMCRegistrationHandler.accept(event); // Register ImmersiveMC's petting handlers last
        didPettingRegistration = true;
    }

    @Override
    public void addPettingHandlerRegistrationHandler(Consumer<ImmersiveMCRegistrationEvent<PettingHandler<?>>> registrationHandler) throws IllegalStateException {
        synchronized (this) {
            if (didPettingRegistration) {
                throw new IllegalStateException("Can't add a registration handler for PettingHandlers after PettingHandlers have been registered.");
            }
            PETTING_HANDLERS.add(registrationHandler);
        }
    }
}
