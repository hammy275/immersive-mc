package com.hammy275.immersivemc.common.compat.apotheosis;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public abstract class ApothBaseCompatImpl implements ApothCompat {

    private Boolean doSuppressVanillaEnchanting = null;

    @Override
    public boolean suppressVanillaEnchanting() {
        // This is a way to check if the enchant module is enabled without touching Apotheosis/Zenith code.
        // Needed in-case ImmersiveMC's compat crashes or the mod isn't present, since we need to prevent ImmersiveMC
        // from preventing vanilla enchanting.
        if (doSuppressVanillaEnchanting == null) {
            // TODO: Below code is not testing as of 1.21.1 -> 1.21.4 port.
            doSuppressVanillaEnchanting = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("apotheosis", "hellshelf")).isPresent() ||
                    BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("zenith", "hellshelf")).isPresent();
        }
        return doSuppressVanillaEnchanting;
    }
}
