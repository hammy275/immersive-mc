package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.client.immersive.AbstractPlayerAttachmentImmersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ClientActiveConfig;
import com.hammy275.immersivemc.common.config.ConfigType;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ConfigSyncPacket;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.hammy275.immersivemc.server.immersive.TrackedImmersives;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;

public class ConfigScreen extends Screen {

    protected final Screen lastScreen;

    private static ConfigType currentConfigAdjusting = ConfigType.CLIENT; // The current type of config being adjusted.

    protected static int BUTTON_WIDTH = 200;
    protected static int BUTTON_HEIGHT = 20;
    protected static int BUTTON_SPACING = BUTTON_HEIGHT + BUTTON_HEIGHT / 5;
    protected static int BUTTON_START_HEIGHT = 24;

    protected static final String WIKI_URL = "https://hammy275.com/immersivemcwiki";

    public ConfigScreen(Screen screen) {
        super(Component.translatable("screen.immersivemc.config.title"));
        this.lastScreen = screen;
    }

    @Override
    protected void init() {
        ActiveConfig.loadActive(); // Load config so we're working with our current values when changing them
        super.init();

        if (currentConfigAdjusting == ConfigType.CLIENT) {
            this.addRenderableWidget(ScreenUtils.createScreenButton(
                    (this.width - BUTTON_WIDTH) / 2, BUTTON_START_HEIGHT,
                    BUTTON_WIDTH, BUTTON_HEIGHT,
                    "config.immersivemc.customize_item_guides",
                    new ItemGuideCustomizeScreen(this)
            ));

            this.addRenderableWidget(ScreenUtils.createScreenButton(
                    (this.width - BUTTON_WIDTH) / 2, BUTTON_START_HEIGHT + BUTTON_SPACING,
                    BUTTON_WIDTH, BUTTON_HEIGHT,
                    "config.immersivemc.backpack",
                    new BackpackConfigScreen(this)
            ));
        }

        this.addRenderableWidget(ScreenUtils.createScreenButton(
                (this.width - BUTTON_WIDTH) / 2, BUTTON_START_HEIGHT + BUTTON_SPACING * 2,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                "config.immersivemc.immersives_customize",
                new ImmersivesCustomizeScreen(this)
        ));

        this.addRenderableWidget(ScreenUtils.createScreenButton(
                (this.width - BUTTON_WIDTH) / 2, BUTTON_START_HEIGHT + BUTTON_SPACING * 3,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                "config.immersivemc.immersives",
                new ImmersivesConfigScreen(this,
                        VRVerify.clientInVR() || currentConfigAdjusting == ConfigType.SERVER ?
                                ImmersivesConfigScreen.ScreenType.BOTH : ImmersivesConfigScreen.ScreenType.NONVR)
        ));

        this.addRenderableWidget(ScreenUtils.createScreenButton(
                (this.width - BUTTON_WIDTH) / 2 - (BUTTON_WIDTH / 2) - 8, this.height - BUTTON_SPACING * 2,
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
                (this.width - BUTTON_WIDTH) / 2, this.height - BUTTON_SPACING,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                this
        ));

        this.addRenderableWidget(ScreenUtils.createButton(
                (this.width - BUTTON_WIDTH) / 2 + (BUTTON_WIDTH / 2) + 8, this.height - BUTTON_SPACING * 2,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                "config.immersivemc.reset",
                (button) -> {
                    if (currentConfigAdjusting == ConfigType.CLIENT) {
                        ActiveConfig.FILE_CLIENT = new ClientActiveConfig();
                    } else {
                        ActiveConfig.FILE_SERVER = new ActiveConfig();
                    }
                    ActiveConfig.loadActive();
                    button.active = false;
                }
        ));

        String configTypeButtonTranslationKey = "config.immersivemc.edit_config_type.server";
        if (currentConfigAdjusting == ConfigType.CLIENT) {
            this.addRenderableWidget(ScreenUtils.createOption("disable_outside_vr", config -> ((ClientActiveConfig) config).disableImmersiveMCOutsideVR, (config, newVal) -> ((ClientActiveConfig) config).disableImmersiveMCOutsideVR = newVal)
                    .createButton(Minecraft.getInstance().options, (this.width - BUTTON_WIDTH) / 2, this.height - BUTTON_SPACING * 4, BUTTON_WIDTH));
            configTypeButtonTranslationKey = "config.immersivemc.edit_config_type.client";
        }
        Button configTypeButton = ScreenUtils.createButton((this.width - BUTTON_WIDTH) / 2, this.height - BUTTON_SPACING * 3,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                configTypeButtonTranslationKey,
                configTypeButtonTranslationKey + ".desc",
                button -> changeConfigAdjusting());
        configTypeButton.active = Minecraft.getInstance().player == null ||
                Minecraft.getInstance().hasSingleplayerServer();
        this.addRenderableWidget(configTypeButton);

    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        onClientConfigChange();
        Minecraft.getInstance().setScreen(lastScreen);
        currentConfigAdjusting = ConfigType.CLIENT;
    }

    public static void onClientConfigChange() {
        writeAdjustingConfig();
        // Clear all immersives in-case we disabled one or adjusted a setting for one
        for (Immersive<?, ?> immersive : Immersives.IMMERSIVES) {
            immersive.getTrackedObjects().clear();
        }
        for (AbstractPlayerAttachmentImmersive<?, ?> immersive : Immersives.IMMERSIVE_ATTACHMENTS) {
            immersive.clearImmersives();
        }
        if (currentConfigAdjusting == ConfigType.SERVER) {
            // If host of a LAN server or playing in singleplayer, send the new config state to other players
            if (Minecraft.getInstance().hasSingleplayerServer()) {
                ActiveConfig.FROM_SERVER = (ActiveConfig) ActiveConfig.FILE_SERVER.clone();
                ActiveConfig.loadActive();
                ActiveConfig.registerPlayerConfig(Minecraft.getInstance().player, ActiveConfig.activeRaw());
                // Propagate config to other players on the server
                IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
                if (server != null && server.isPublished()) {
                    server.getPlayerList().getPlayers().stream()
                            .filter((player) -> !player.getUUID().equals(Minecraft.getInstance().player.getUUID()))
                            .forEach(ConfigSyncPacket::syncConfigToPlayer);
                }
                TrackedImmersives.clearForPlayer(Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayer(Minecraft.getInstance().player.getUUID()));
            }
        } else if (Minecraft.getInstance().level != null) {
            // Load config into active, and let server know of our new config state
            ActiveConfig.loadActive();
            Network.INSTANCE.sendToServer(new ConfigSyncPacket(ActiveConfig.FILE_CLIENT));
        }

    }

    /**
     * Switch between adjusting the client and server configuration.
     */
    private static void changeConfigAdjusting() {
        onClientConfigChange();
        currentConfigAdjusting = currentConfigAdjusting == ConfigType.CLIENT ? ConfigType.SERVER : ConfigType.CLIENT;
        Screen current = Minecraft.getInstance().screen;
        if (current instanceof ConfigScreen cs) {
            Minecraft.getInstance().setScreen(new ConfigScreen(cs.lastScreen));
        }
    }

    /**
     * @return The current configuration being adjusted.
     */
    public static ActiveConfig getAdjustingConfig() {
        return ActiveConfig.getFileConfig(currentConfigAdjusting);
    }

    /**
     * @return The current type of config being adjusted.
     */
    public static ConfigType getAdjustingConfigType() {
        return currentConfigAdjusting;
    }

    /**
     * Gets the client file config if it's being adjusted, or throws an exception if it's not being adjusted.
     * <br>
     * Better to use this than to directly get {@link ActiveConfig#FILE_CLIENT}, so we can verify the config screens
     * behave correctly when editing the server config.
     * @return The client file config.
     */
    public static ClientActiveConfig getClientConfigIfAdjusting() {
        if (currentConfigAdjusting == ConfigType.CLIENT) {
            return (ClientActiveConfig) ActiveConfig.getFileConfig(ConfigType.CLIENT);
        }
        throw new IllegalStateException("Not currently adjusting client config!");
    }

    public static boolean writeAdjustingConfig() {
        return ActiveConfig.getFileConfig(currentConfigAdjusting).writeConfigFile(currentConfigAdjusting);
    }
}
