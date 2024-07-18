package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.client.immersive.AbstractPlayerAttachmentImmersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ImmersiveMCConfig;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ConfigSyncPacket;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.server.immersive.TrackedImmersives;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class ConfigScreen extends Screen {

    protected final Screen lastScreen;

    protected static int BUTTON_WIDTH = 200;
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

        this.addRenderableWidget(ScreenUtils.createScreenButton(
                (this.width - BUTTON_WIDTH) / 2, this.height / 2 - BUTTON_HEIGHT - 72,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                "config.immersivemc.customize_item_guides",
                new ItemGuideCustomizeScreen(this)
        ));

        this.addRenderableWidget(ScreenUtils.createScreenButton(
                (this.width - BUTTON_WIDTH) / 2, this.height / 2 - BUTTON_HEIGHT - 40,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                "config.immersivemc.backpack",
                new BackpackConfigScreen(this)
        ));

        this.addRenderableWidget(ScreenUtils.createScreenButton(
                (this.width - BUTTON_WIDTH) / 2, this.height / 2 - BUTTON_HEIGHT - 8,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                "config.immersivemc.immersives_customize",
                new ImmersivesCustomizeScreen(this)
        ));

        this.addRenderableWidget(ScreenUtils.createScreenButton(
                (this.width - BUTTON_WIDTH) / 2, this.height / 2 - BUTTON_HEIGHT + 24,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                "config.immersivemc.immersives",
                new ImmersivesConfigScreen(this,
                        VRPluginVerify.clientInVR() ?
                                ImmersivesConfigScreen.ScreenType.BOTH : ImmersivesConfigScreen.ScreenType.NONVR)
        ));

        this.addRenderableWidget(ScreenUtils.createScreenButton(
                (this.width - BUTTON_WIDTH) / 2, this.height - 40 - BUTTON_HEIGHT * 2,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                "config.immersivemc.wiki_button",
                new ConfirmLinkScreen((clickedYes) -> {
                    if (clickedYes) {
                        Util.getPlatform().openUri(WIKI_URL);
                    }
                    Minecraft.getInstance().setScreen(this);
                }, Component.translatable("config.immersivemc.open_wiki_message"),
                        Component.empty(),
                        WIKI_URL,
                        CommonComponents.GUI_CANCEL,
                        true)
        ));

        this.addRenderableWidget(ScreenUtils.createDoneButton(
                (this.width - BUTTON_WIDTH) / 2, this.height - 26,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                this
        ));

        this.addRenderableWidget(ScreenUtils.createButton(
                (this.width - BUTTON_WIDTH) / 2 + (BUTTON_WIDTH / 2) + 8, this.height - 32 - BUTTON_HEIGHT,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                "config.immersivemc.reset",
                (button) -> {
                    ImmersiveMCConfig.resetToDefault();
                    ActiveConfig.loadActive();
                    button.active = false;
                }
        ));

        this.addRenderableWidget(ScreenUtils.createOption("disable_outside_vr", ImmersiveMCConfig.disableOutsideVR)
                .createButton(Minecraft.getInstance().options, (this.width - BUTTON_WIDTH) / 2 - (BUTTON_WIDTH / 2) - 8, this.height - 32 - BUTTON_HEIGHT, BUTTON_WIDTH));

    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);

        graphics.drawCenteredString(this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        boolean isSingleplayerHost = Minecraft.getInstance().hasSingleplayerServer();
        ActiveConfig.FILE.loadFromFile();
        if (isSingleplayerHost) {
            ActiveConfig.FROM_SERVER = (ActiveConfig) ActiveConfig.FILE.clone();
        }
        ActiveConfig.loadActive();
        // Clear all immersives in-case we disabled one
        for (Immersive<?, ?> immersive : Immersives.IMMERSIVES) {
            immersive.getTrackedObjects().clear();
        }
        for (AbstractPlayerAttachmentImmersive<?, ?> immersive : Immersives.IMMERSIVE_ATTACHMENTS) {
            immersive.clearImmersives();
        }
        if (isSingleplayerHost) {
            // If host of a LAN server or playing in singleplayer, send the new config state to other players
            ActiveConfig.registerPlayerConfig(Minecraft.getInstance().player, ActiveConfig.activeRaw()); // Register our config in the server map

            // Propagate config to other players on the server
            IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server != null && server.isPublished()) {
                List<ServerPlayer> allButHost = server.getPlayerList().getPlayers().stream()
                        .filter((player) -> !player.getUUID().equals(Minecraft.getInstance().player.getUUID()))
                        .toList();
                Network.INSTANCE.sendToPlayers(allButHost, new ConfigSyncPacket(ActiveConfig.FILE));
            }
            TrackedImmersives.clearForPlayer(Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayer(Minecraft.getInstance().player.getUUID()));
        } else if (Minecraft.getInstance().level != null) {
            // Let server know of our new config state
            Network.INSTANCE.sendToServer(new ConfigSyncPacket(ActiveConfig.FILE));
        }
        Minecraft.getInstance().setScreen(lastScreen);
    }
}
