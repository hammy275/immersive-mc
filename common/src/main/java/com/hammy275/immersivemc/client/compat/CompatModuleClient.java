package com.hammy275.immersivemc.client.compat;

import com.hammy275.immersivemc.client.config.screen.ConfigScreen;
import com.hammy275.immersivemc.common.compat.util.CompatModule;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import net.minecraft.client.Minecraft;

import java.util.function.BiConsumer;

public class CompatModuleClient {

    public static void disableClient(String friendlyName, BiConsumer<ActiveConfig, Boolean> configSetter) {
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            // Host of a LAN world or in singleplayer. Assume it's a server-wide issue, so we don't have to thread-guess.
            // Only time this creates something unideal is if we're the host of a LAN world others are playing on.
            CompatModule.handleDisableServer(friendlyName, configSetter, Minecraft.getInstance().getSingleplayerServer());
        } else {
            // On a multiplayer server, so we're definitely on the client thread.
            handleDisableClient(friendlyName, configSetter);
        }
    }

    private static void handleDisableClient(String friendlyName, BiConsumer<ActiveConfig, Boolean> configSetter) {
        configSetter.accept(ActiveConfig.FILE_CLIENT, false);
        ConfigScreen.onClientConfigChange();
        Minecraft.getInstance().player.sendSystemMessage(CompatModule.getErrorMessage(friendlyName));
    }
}
