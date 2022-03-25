package net.blf02.immersivemc.client.config;

public class ClientConfig {

    // How long the overlay should be displayed
    public static final int ticksToRenderFurnace = 80;
    public static final int ticksToRenderBrewing = 120;
    public static final int ticksToRenderCrafting = 120;

    // Size of items when rendered in front of something immersive
    public static final float itemScaleSizeFurnace = 0.5f;
    public static final float itemScaleSizeBrewing = 1f/3f;
    public static final float itemScaleSizeCrafting = 0.25f;

    // Time to transition in ticks
    public static final int transitionTime = 20;
}
