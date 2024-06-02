package com.hammy275.immersivemc.api.common;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.common.api_impl.ImmersiveMCRegistrationImpl;

public interface ImmersiveMCRegistration {

    /**
     * @return An ImmersiveMCRegistration instance to access API functions. All details of this object not
     *         mentioned in this file are assumed to be an implementation detail, and may change at any time.
     */
    public static ImmersiveMCRegistration instance() {
        return ImmersiveMCRegistrationImpl.INSTANCE;
    }

    /**
     * Registers a handler for handling (block) Immersives.
     * @param handler The handler to register.
     * @throws IllegalArgumentException If the handler is already registered.
     */
    public void registerImmersiveHandler(ImmersiveHandler<?> handler) throws IllegalArgumentException;
    
}
