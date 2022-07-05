package net.blf02.immersivemc.common.config;

import net.minecraft.client.resources.I18n;

public enum PlacementMode {
    PLACE_ONE,
    PLACE_QUARTER,
    PLACE_HALF,
    PLACE_ALL;

    public int toInt() {
        return this.ordinal();
    }

    public static PlacementMode fromInt(int ordinal) {
        return PlacementMode.values()[ordinal];
    }


    @Override
    public String toString() {
        return I18n.get("config.immersivemc.placement_mode." + this.ordinal());
    }
}
