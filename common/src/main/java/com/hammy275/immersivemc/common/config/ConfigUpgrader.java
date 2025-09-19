package com.hammy275.immersivemc.common.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hammy275.immersivemc.common.config.ActiveConfig.COMMON_CONFIG_VERSION;
import static com.hammy275.immersivemc.common.config.ClientActiveConfig.CLIENT_CONFIG_VERSION;

/**
 * Handles upgrading between config formats for both common and client configs.
 * Stored here as to not clutter the main config class files.
 * <br>
 * IMPORTANT: Since JSON only understands decimal numbers, all integers are doubles.
 */
public class ConfigUpgrader {

    public static boolean upgradeCommonIfNeeded(Map<Object, Object> config) {
        int version = config.containsKey(COMMON_CONFIG_VERSION) ? readInt(config, COMMON_CONFIG_VERSION) : 1;
        boolean didUpgrade = version < ActiveConfig.DEFAULT.commonConfigVersion;
        while (version < ActiveConfig.DEFAULT.commonConfigVersion) {
            if (version == 1) {
                // Add version value
                config.put(COMMON_CONFIG_VERSION, 1);
            } else if (version == 2) {
                // Remove shield Immersive setting since it's no longer used
                config.remove("useShieldImmersive");
            }

            version++;
        }
        config.put(COMMON_CONFIG_VERSION, version);
        return didUpgrade;
    }

    public static boolean upgradeClientIfNeeded(Map<Object, Object> config) {
        int version = config.containsKey(CLIENT_CONFIG_VERSION) ? readInt(config, CLIENT_CONFIG_VERSION) : 1;
        boolean didUpgrade = version < ClientActiveConfig.DEFAULT.clientConfigVersion;
        while (version < ClientActiveConfig.DEFAULT.clientConfigVersion) {
            if (version == 1) {
                // Add version value
                config.put(CLIENT_CONFIG_VERSION, 1);
            } else if (version == 2) {
                // Convert up crouchingBypassesImmersives to crouchMode
                boolean bypassImmersive = (boolean) config.getOrDefault("crouchingBypassesImmersives", false);
                config.put("crouchMode", bypassImmersive ? CrouchMode.BYPASS_IMMERSIVE.toString() : CrouchMode.SWAP_ALL.toString());
                config.remove("crouchingBypassesImmersives");
            } else if (version == 3) {
                // Convert up colors to modern system
                int itemGuideColor = readInt(config, "itemGuideColor");
                int itemGuideSelectedColor = readInt(config, "itemGuideSelectedColor");
                int rangedGrabColor = readInt(config, "rangedGrabColor");
                if (itemGuideColor == 0x3300ffff && itemGuideSelectedColor == 0x3300ff00 && rangedGrabColor == 0xff00ffff) {
                    config.put("itemGuidePreset", ItemGuidePreset.CLASSIC.name());
                } else if (itemGuideColor == 0x638b8b8b && itemGuideSelectedColor == 0x7fc5c5c5 && rangedGrabColor == 0xffc5c5c5) {
                    config.put("itemGuidePreset", ItemGuidePreset.GRAY.name());
                } else if (itemGuideColor != -1 && itemGuideSelectedColor != -1 && rangedGrabColor != -1) {
                    config.put("itemGuidePreset", ItemGuidePreset.CUSTOM.name());
                    Map<Object, Object> customColorMap = new HashMap<>();
                    customColorMap.put("colors", List.of(itemGuideColor));
                    customColorMap.put("selectedColors", List.of(itemGuideSelectedColor));
                    customColorMap.put("rangedGrabColors", List.of(rangedGrabColor));
                    customColorMap.put("transitionTimeMS", 5000);
                    config.put("itemGuideCustomColorData", customColorMap);
                }
            }

            version++;
        }
        config.put(CLIENT_CONFIG_VERSION, version);
        return didUpgrade;
    }

    private static int readInt(Map<Object, Object> config, String key) {
        return ((Double) config.get(key)).intValue();
    }

}
