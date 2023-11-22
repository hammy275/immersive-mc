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
    public static final int defaultTicksToRender = 120;
    public static final int ticksToRenderFurnace = 80;
    public static final int ticksToRenderBrewing = 120;
    public static final int ticksToRenderCrafting = 120;
    public static final int ticksToHandleJukebox = 100;
    public static final int ticksToRenderChest = Integer.MAX_VALUE; // You manually close a chest
    public static final int ticksToRenderAnvil = 120;
    public static final int ticksToRenderETable = 60*20; // 1 min
    public static final int ticksToRenderBackpack = Integer.MAX_VALUE; // You dismiss the backpack manually
    public static final int ticksToRenderRepeater = 80; // We're always pointing at one, so we don't need it much longer after that
    public static final int ticksToRenderShulker = 300;
    public static final int ticksToRenderBeacon = 60*20;
    public static final int ticksToRenderBarrel = ticksToRenderChest;
    public static final int ticksToRenderHopper = 100;
    public static final int ticksToRenderHitboxesImmersive = Integer.MAX_VALUE;
    public static final int ticksToRenderSmithingTable = ticksToRenderAnvil;
    public static final int ticksToRenderChiseledBookshelf = 300;

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
    public static final int transitionTime = 20;

    // Amount of ticks between syncs (sync every `inventorySyncTime` ticks)
    public static final int inventorySyncTime = 2;

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
