package com.hammy275.immersivemc.client.config;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientConstants {
    // Mixin Reflection Constants
    public static final Class<?> hotswitchVivecraftItemRenderingClass;


    // How long the overlay should be displayed
    public static final int ticksToRenderChest = Integer.MAX_VALUE; // You manually close a chest
    public static final int ticksToRenderAnvil = Integer.MAX_VALUE;
    public static final int ticksToRenderBackpack = Integer.MAX_VALUE; // You dismiss the backpack manually
    public static final int ticksToRenderRepeater = Integer.MAX_VALUE; // We're always pointing at one, so we don't need it much longer after that
    public static final int ticksToRenderBeacon = Integer.MAX_VALUE;
    public static final int ticksToRenderHitboxesImmersive = Integer.MAX_VALUE;
    public static final int ticksToRenderLever = Integer.MAX_VALUE;

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

    // Time to transition in ticks
    public static final int transitionTime = 10;
    public static final float transitionMult = 1f / transitionTime;

    // Default cooldown time for interactions for desktop users
    public static final int defaultCooldownTicks = 8;
    // Multiplier for the cooldown time in ticks for VR users
    public static final double cooldownVRMultiplier = 1.5;
    public static final float sizeScaleForHover = 1.25f;


    // Enchanting table y offsets for item animations and a map storing enchantments for fake items to have glimmer
    public static final List<Float> eTableYOffsets = new ArrayList<>();
    public static final Map<Enchantment, Integer> fakeEnch = new HashMap<>();

    static {
        float max = 0.25f;
        for (float i = 0; i <= max; i += max / 20f) {
            eTableYOffsets.add(i - (max / 2f));
        }
        for (float i = 0.25f; i >= 0f; i -= max / 20f) {
            eTableYOffsets.add(i - (max / 2f));
        }
        fakeEnch.put(Enchantments.MENDING, 32767);

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
