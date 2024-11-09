package com.hammy275.immersivemc.common.compat.apotheosis;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AirItem;

public abstract class ApothBaseCompatImpl implements ApothCompat {

    private Boolean doSuppressVanillaEnchanting = null;

    @Override
    public boolean suppressVanillaEnchanting() {
        // This is a way to check if the enchant module is enabled without touching Apotheosis/Zenith code.
        // Needed in-case ImmersiveMC's compat crashes or the mod isn't present, since we need to prevent ImmersiveMC
        // from preventing vanilla enchanting.
        if (doSuppressVanillaEnchanting == null) {
            doSuppressVanillaEnchanting = !(BuiltInRegistries.ITEM.get(new ResourceLocation("apotheosis", "hellshelf")) instanceof AirItem) ||
                    !(BuiltInRegistries.ITEM.get(new ResourceLocation("zenith", "hellshelf")) instanceof AirItem);
        }
        return doSuppressVanillaEnchanting;
    }
}
