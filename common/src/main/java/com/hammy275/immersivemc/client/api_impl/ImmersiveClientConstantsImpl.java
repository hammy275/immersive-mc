package com.hammy275.immersivemc.client.api_impl;

import com.hammy275.immersivemc.api.client.ImmersiveClientConstants;
import com.hammy275.immersivemc.client.config.ClientConstants;

public class ImmersiveClientConstantsImpl implements ImmersiveClientConstants {

    public static final ImmersiveClientConstants INSTANCE = new ImmersiveClientConstantsImpl();

    @Override
    public int defaultCooldown() {
        return ClientConstants.defaultCooldownTicks;
    }

    @Override
    public int growTransitionTime() {
        return ClientConstants.transitionTime;
    }

    @Override
    public float hoverScaleMultiplier() {
        return ClientConstants.sizeScaleForHover;
    }
}
