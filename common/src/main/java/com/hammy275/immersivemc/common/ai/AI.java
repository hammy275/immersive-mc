package com.hammy275.immersivemc.common.ai;

import net.minecraft.client.Minecraft;

import java.util.Random;

public class AI {

    private static final Random myAI = new Random(Minecraft.getInstance().getGameProfile().getName().hashCode());

    public static Random ai() {
        // TODO: Actually use AI instead of RNG
        return myAI;
    }
}
