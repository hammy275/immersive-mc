package com.hammy275.immersivemc.client.compat;

import com.hammy275.immersivemc.client.config.screen.ConfigScreen;
import com.hammy275.immersivemc.common.compat.CompatData;
import com.hammy275.immersivemc.common.compat.util.CompatModule;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import net.minecraft.client.Minecraft;

public class CompatModuleClient {

    public static void disableClient(CompatData compatData) {
        boolean noMessage = false;
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            // Host of a LAN world or in singleplayer. Assume it's a both-side issue, so we don't have to thread-guess.
            // Only time this creates something unideal is if we're the host of a LAN world others are playing on.
            CompatModule.handleDisableServer(compatData, Minecraft.getInstance().getSingleplayerServer());
            noMessage = true; // Message is done by server disabling
        }
        handleDisableClient(compatData, noMessage);
    }

    private static void handleDisableClient(CompatData compatData, boolean noMessage) {
        compatData.configSetter().accept(ActiveConfig.FILE_CLIENT, false);
        ConfigScreen.onClientConfigChange();
        if (!noMessage) {
            Minecraft.getInstance().player.displayClientMessage(CompatModule.getErrorMessage(compatData.friendlyName()), false);
        }
    }
}
