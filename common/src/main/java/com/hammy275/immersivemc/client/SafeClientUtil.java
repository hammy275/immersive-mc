package com.hammy275.immersivemc.client;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.common.config.PlacementMode;

// Like ClientUtil, but can be safely called from the server thread without import problems
public class SafeClientUtil {

    public static PlacementMode getPlacementMode() {
        if (Platform.isClient()) {
            return ClientUtil.getPlacementModeIndirect();
        } else {
            return null;
        }
    }

    public static PlacementMode getPlacementMode(boolean leftClickAlreadyDoesSomething) {
        if (Platform.isClient()) {
            return ClientUtil.getPlacementModeIndirect(leftClickAlreadyDoesSomething);
        } else {
            return null;
        }
    }
}
