package com.hammy275.immersivemc.common.api_impl;

import com.hammy275.immersivemc.api.common.ImmersiveMCRegistration;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;

public class ImmersiveMCRegistrationImpl implements ImmersiveMCRegistration {

    public static final ImmersiveMCRegistration INSTANCE = new ImmersiveMCRegistrationImpl();

    @Override
    public void registerImmersiveHandler(ImmersiveHandler handler) throws IllegalArgumentException {
        if (ImmersiveHandlers.HANDLERS.contains(handler)) {
            throw new IllegalArgumentException("Handler %s already registered.".formatted(handler.toString()));
        }
        ImmersiveHandlers.HANDLERS.add(0, handler);
    }
}
