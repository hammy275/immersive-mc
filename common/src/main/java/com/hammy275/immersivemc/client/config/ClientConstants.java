package com.hammy275.immersivemc.client.config;

public class ClientConstants {
    // Mixin Reflection Constants
    public static final Class<?> hotswitchVivecraftItemRenderingClass;


    // How long the overlay should be displayed
    public static final int ticksToRenderBackpack = Integer.MAX_VALUE; // You dismiss the backpack manually
    public static final int ticksToRenderHitboxesImmersive = Integer.MAX_VALUE;

    // Size of items when rendered in front of something immersive
    public static final float defaultItemScaleSize = 1f/3f;
    public static final float itemScaleSizeFurnace = 0.5f;
    public static final float itemScaleSizeBrewing = 1f/3f;
    public static final float itemScaleSizeCrafting = 3f/16f; // Chosen for the texture of the table itself
    public static final float itemScaleSizeChest = 0.25f;
    public static final float itemScaleSizeAnvil = 0.3333f; // Intentionally NOT 1f/3f so item guide boxes don't overlap
    public static final float itemScaleSizeETable = 0.42f;
    public static final float itemScaleSizeBackpack = 0.2f;
    public static final float itemScaleSizeBackpackSelected = 0.3f;
    public static final float itemScaleSizeShulker = 0.15f;
    public static final float itemScaleSizeBeacon = 0.42f;
    public static final float itemScaleSizeBarrel = 0.2f;
    public static final float itemScaleSizeHopper = 0.15f;
    public static final float itemScaleSizeSmithingTable = itemScaleSizeAnvil;
    public static final float itemScaleSizeApothSalvagingTable = 0.3f;
    public static final float itemScaleSizeGrindstone = 0.25f;

    // Time to transition in ticks
    public static final int transitionTime = 10;
    public static final float transitionMult = 1f / transitionTime;

    // Default cooldown time for interactions for desktop users
    public static final int defaultCooldownTicks = 8;
    // Multiplier for the cooldown time in ticks for VR users
    public static final double cooldownVRMultiplier = 1.5;
    public static final float sizeScaleForHover = 1.25f;

    static {
        hotswitchVivecraftItemRenderingClass = getClassOrNull("org.vivecraft.client_vr.render.VivecraftItemRendering");
    }

    private static Class<?> getClassOrNull(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
