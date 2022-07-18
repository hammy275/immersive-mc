package net.blf02.immersivemc.client;

import net.blf02.immersivemc.common.config.PlacementMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;

// Like ClientUtil, but can be safely called from the server thread without import problems
public class SafeClientUtil {

    public static PlacementMode getPlacementMode() {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            return ClientUtil.getPlacementModeIndirect();
        } else {
            return null;
        }
    }
}
