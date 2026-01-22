package com.hammy275.immersivemc.client.workaround;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

public class ClickHandlerScreen extends Screen {

    private boolean closeWhenAble = false;

    public ClickHandlerScreen() {
        super(Component.empty());
    }

    public boolean handleComponentClicked(@Nullable Style style) {
        if (style != null && style.getClickEvent() != null) {
            Screen.defaultHandleClickEvent(style.getClickEvent(), Minecraft.getInstance(), this);
            ClickEvent.Action action = style.getClickEvent().action();
            if (action == ClickEvent.Action.RUN_COMMAND || action == ClickEvent.Action.COPY_TO_CLIPBOARD ||
                action == ClickEvent.Action.SUGGEST_COMMAND) {
                // SUGGEST_COMMAND seems to not work in vanilla, so no need to make it work here
                Minecraft.getInstance().setScreen(null);
            } else if (action == ClickEvent.Action.OPEN_URL) {
                this.closeWhenAble = true; // Need to wait until this screen is restored to close
            }
            return true;
        }
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (closeWhenAble) {
            Minecraft.getInstance().setScreen(null); // Hacky, but Screens don't have a tick()
        }
    }
}
