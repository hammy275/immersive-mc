package com.hammy275.immersivemc.client.api_impl;

import com.hammy275.immersivemc.api.client.ImmersiveMCClientRegistration;
import com.hammy275.immersivemc.api.client.immersive.Immersive;

public class ImmersiveMCClientRegistrationImpl implements ImmersiveMCClientRegistration {

    public static final ImmersiveMCClientRegistration INSTANCE = new ImmersiveMCClientRegistrationImpl();

    @Override
    public void registerImmersive(Immersive<?, ?> immersive) throws IllegalArgumentException {

    }
}
