package com.hammy275.immersivemc.client.config.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class Buttons {

    public static Button.Builder getScreenButton(Screen screen, String translationString) {
        return Button.builder(Component.translatable(translationString), (button) -> Minecraft.getInstance().setScreen(screen));
    }
}
