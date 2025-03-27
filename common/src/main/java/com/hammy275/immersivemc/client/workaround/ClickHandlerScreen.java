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

    @Override
    public boolean handleComponentClicked(@Nullable Style style) {
        boolean ret = super.handleComponentClicked(style);
        if (style != null && style.getClickEvent() != null) {
            ClickEvent.Action action = style.getClickEvent().action();
            if (action == ClickEvent.Action.RUN_COMMAND || action == ClickEvent.Action.COPY_TO_CLIPBOARD ||
                action == ClickEvent.Action.SUGGEST_COMMAND) {
                // SUGGEST_COMMAND seems to not work in vanilla, so no need to make it work here
                Minecraft.getInstance().setScreen(null);
            } else if (action == ClickEvent.Action.OPEN_URL) {
                this.closeWhenAble = true; // Need to wait until this screen is restored to close
            }
        }
        return ret;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (closeWhenAble) {
            Minecraft.getInstance().setScreen(null); // Hacky, but Screens don't have a tick()
        }
    }
}
