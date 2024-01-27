package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.client.immersive.AbstractImmersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ImmersiveMCConfig;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ConfigSyncPacket;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class ConfigScreen extends Screen {

    protected final Screen lastScreen;

    protected static int BUTTON_WIDTH = 160;
    protected static int BUTTON_HEIGHT = 20;

    protected static final String WIKI_URL = "https://hammy275.com/immersivemcwiki";

    public ConfigScreen(Screen screen) {
        super(Component.translatable("screen.immersivemc.config.title"));
        this.lastScreen = screen;
    }

    @Override
    protected void init() {
        ActiveConfig.loadActive(); // Load config so we're working with our current values when changing them
        super.init();

        this.addRenderableWidget(Buttons.getScreenButton(new ItemGuideCustomizeScreen(this),
                        "config.immersivemc.customize_item_guides")
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .pos((this.width - BUTTON_WIDTH) / 2, this.height / 2 - BUTTON_HEIGHT - 64)
                .build());

        this.addRenderableWidget(Buttons.getScreenButton(new BackpackConfigScreen(this),
                "config.immersivemc.backpack")
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .pos((this.width - BUTTON_WIDTH) / 2, this.height / 2 - BUTTON_HEIGHT - 32)
                .build());

        this.addRenderableWidget(Buttons.getScreenButton(new ImmersivesCustomizeScreen(this),
                "config.immersivemc.immersives_customize")
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .pos((this.width - BUTTON_WIDTH) / 2, this.height / 2 - BUTTON_HEIGHT)
                .build());

        this.addRenderableWidget(Buttons.getScreenButton(new ImmersivesConfigScreen(this,
                        VRPluginVerify.clientInVR() ?
                        ImmersivesConfigScreen.ScreenType.BOTH : ImmersivesConfigScreen.ScreenType.NONVR),
                "config.immersivemc.immersives")
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .pos((this.width - BUTTON_WIDTH) / 2, this.height / 2 - BUTTON_HEIGHT + 32)
                .build());

        this.addRenderableWidget(Buttons.getScreenButton(new ConfirmLinkScreen((clickedYes) -> {
            if (clickedYes) {
                Util.getPlatform().openUri(WIKI_URL);
            }
            Minecraft.getInstance().setScreen(this);
        }, Component.translatable("config.immersivemc.open_wiki_message"),
                Component.empty(),
                WIKI_URL,
                CommonComponents.GUI_CANCEL,
                true), "config.immersivemc.wiki_button")
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .pos((this.width - BUTTON_WIDTH) / 2, this.height - BUTTON_HEIGHT - 26 - 16)
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                (button) -> this.onClose())
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .pos((this.width - BUTTON_WIDTH) / 2 - (BUTTON_WIDTH / 2) - 8, this.height - 26)
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable("config.immersivemc.reset"),
                (button) -> {
                    ImmersiveMCConfig.resetToDefault();
                    ActiveConfig.loadActive();
                    button.active = false;
                })
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .pos((this.width - BUTTON_WIDTH) / 2 + (BUTTON_WIDTH / 2) + 8, this.height - 26)
                .build());

    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);

        graphics.drawCenteredString(this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        ActiveConfig.FILE.loadFromFile();
        ActiveConfig.loadActive();
        // Clear all immersives in-case we disabled one
        for (AbstractImmersive<?> immersive : Immersives.IMMERSIVES) {
            immersive.clearImmersives();
        }
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            // If host of a LAN server or playing in singleplayer, have server and client reload config, and send
            // new config state to other players
            ActiveConfig.FILE.loadFromFile(); // Load config for server-side
            ActiveConfig.ACTIVE.loadFromFile(); // Load config for client-side. Okay to do, since in this case, the client and server are the same config

            // Propagate config to other players on the server
            IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server != null && server.isPublished()) {
                List<ServerPlayer> allButHost = server.getPlayerList().getPlayers().stream()
                        .filter((player) -> !player.getUUID().equals(Minecraft.getInstance().player.getUUID()))
                        .toList();
                Network.INSTANCE.sendToPlayers(allButHost, new ConfigSyncPacket(ActiveConfig.FILE));
            }
        } else if (Minecraft.getInstance().level != null) {
            // Let server know of our new config state
            Network.INSTANCE.sendToServer(new ConfigSyncPacket(ActiveConfig.FILE));
        }
        Minecraft.getInstance().setScreen(lastScreen);
    }
}
