package com.hammy275.immersivemc.api.client;

import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.client.api_impl.ImmersiveMCClientRegistrationImpl;

public interface ImmersiveMCClientRegistration {

    /**
     * @return An ImmersiveMCClientRegistration instance to access API functions. All details of this object not
     *         mentioned in this file are assumed to be an implementation detail, and may change at any time.
     */
    public static ImmersiveMCClientRegistration instance() {
        return ImmersiveMCClientRegistrationImpl.INSTANCE;
    }

    /**
     * Register a block Immersive.
     * @param immersive Immersive to register.
     * @throws IllegalArgumentException If the Immersive is already registered.
     */
    public void registerImmersive(Immersive<?, ?> immersive) throws IllegalArgumentException;

}
