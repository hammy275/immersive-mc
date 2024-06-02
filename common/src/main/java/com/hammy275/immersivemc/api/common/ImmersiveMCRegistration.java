package com.hammy275.immersivemc.api.common;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;

public interface ImmersiveMCRegistration {

    /**
     * Registers a handler for handling (block) Immersives.
     * @param handler The handler to register.
     * @throws IllegalArgumentException If the handler is already registered.
     */
    public void registerImmersiveHandler(ImmersiveHandler handler) throws IllegalArgumentException;
    
}
