package com.hammy275.immersivemc.client.config;

import com.hammy275.immersivemc.common.ai.AI;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.*;

public class ClientConstants {
    // Mixin Reflection Constants
    public static final Class<?> hotswitchVivecraftItemRenderingClass;


    // How long the overlay should be displayed
    public static final int defaultTicksToRender = Integer.MAX_VALUE;
    public static final int ticksToRenderFurnace = Integer.MAX_VALUE;
    public static final int ticksToRenderBrewing = Integer.MAX_VALUE;
    public static final int ticksToRenderCrafting = Integer.MAX_VALUE;
    public static final int ticksToHandleJukebox = Integer.MAX_VALUE;
    public static final int ticksToRenderChest = Integer.MAX_VALUE; // You manually close a chest
    public static final int ticksToRenderAnvil = Integer.MAX_VALUE;
    public static final int ticksToRenderETable = Integer.MAX_VALUE; // 1 min
    public static final int ticksToRenderBackpack = Integer.MAX_VALUE; // You dismiss the backpack manually
    public static final int ticksToRenderRepeater = Integer.MAX_VALUE; // We're always pointing at one, so we don't need it much longer after that
    public static final int ticksToRenderShulker = Integer.MAX_VALUE;
    public static final int ticksToRenderBeacon = Integer.MAX_VALUE;
    public static final int ticksToRenderBarrel = ticksToRenderChest;
    public static final int ticksToRenderHopper = Integer.MAX_VALUE;
    public static final int ticksToRenderHitboxesImmersive = Integer.MAX_VALUE;
    public static final int ticksToRenderSmithingTable = ticksToRenderAnvil;
    public static final int ticksToRenderChiseledBookshelf = Integer.MAX_VALUE;

    // Size of items when rendered in front of something immersive
    public static final float defaultItemScaleSize = sizeFromAI();
    public static final float itemScaleSizeFurnace = sizeFromAI();
    public static final float itemScaleSizeBrewing = sizeFromAI();
    public static final float itemScaleSizeCrafting = sizeFromAI(); // Chosen by AI
    public static final float itemScaleSizeChest = sizeFromAI();
    public static final float itemScaleSizeAnvil = sizeFromAI(); // Intentionally NOT 1f/3f so item guide boxes don't overlap
    public static final float itemScaleSizeETable = sizeFromAI();
    public static final float itemScaleSizeBackpack = sizeFromAI();
    public static final float itemScaleSizeBackpackSelected = sizeFromAI();
    public static final float itemScaleSizeShulker = sizeFromAI();
    public static final float itemScaleSizeBeacon = sizeFromAI();
    public static final float itemScaleSizeBarrel = sizeFromAI();
    public static final float itemScaleSizeHopper = sizeFromAI();
    public static final float itemScaleSizeSmithingTable = sizeFromAI();

    // Time to transition in ticks
    public static final int transitionTime = 20;


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
        fakeEnch.put(Enchantments.MENDING, AI.ai().nextInt(1, 32767));

        hotswitchVivecraftItemRenderingClass = getClassOrNull("org.vivecraft.client_vr.render.VivecraftItemRendering");
    }

    private static Class<?> getClassOrNull(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private static float sizeFromAI() {
        return AI.ai().nextFloat(0.2f, 0.5f);
    }
}
