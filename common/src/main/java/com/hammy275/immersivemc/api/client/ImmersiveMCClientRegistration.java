package com.hammy275.immersivemc.api.client;

import com.hammy275.immersivemc.api.client.immersive.Immersive;

public interface ImmersiveMCClientRegistration {

    /**
     * Register a block Immersive.
     * @param immersive Immersive to register.
     * @throws IllegalArgumentException If the Immersive is already registered.
     */
    public void registerImmersive(Immersive<?> immersive) throws IllegalArgumentException;

}
