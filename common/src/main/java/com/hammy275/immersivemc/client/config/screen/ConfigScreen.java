package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ImmersiveMCConfig;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

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
        ActiveConfig.loadConfigFromFile(); // Load config so we're working with our current values when changing them
        super.init();

        this.addRenderableWidget(new Button(
                (this.width - BUTTON_WIDTH) / 2, this.height / 2 - BUTTON_HEIGHT - 32,
                BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable("config.immersivemc.customize_item_guides"),
                (button) -> Minecraft.getInstance().setScreen(new ItemGuideCustomizeScreen(this))
        ));

        this.addRenderableWidget(new Button(
                (this.width - BUTTON_WIDTH) / 2, this.height / 2 - BUTTON_HEIGHT - 16,
                BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable("config.immersivemc.backpack"),
                (button) -> Minecraft.getInstance().setScreen(new BackpackConfigScreen(this))
        ));

        this.addRenderableWidget(new Button(
                (this.width - BUTTON_WIDTH) / 2, this.height / 2,
                BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable("config.immersivemc.immersives_customize"),
                (button) -> Minecraft.getInstance().setScreen(new ImmersivesCustomizeScreen(this))
        ));

        this.addRenderableWidget(new Button(
                (this.width - BUTTON_WIDTH) / 2, this.height / 2 + BUTTON_HEIGHT + 16,
                BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable("config.immersivemc.immersives"),
                (button) -> Minecraft.getInstance().setScreen(new ImmersivesConfigScreen(this,
                        VRPluginVerify.hasAPI ?
                                ImmersivesConfigScreen.ScreenType.BOTH : ImmersivesConfigScreen.ScreenType.NONVR))
        ));

        this.addRenderableWidget(new Button(
                (this.width - BUTTON_WIDTH) / 2, this.height - 26 - 32,
                BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable("config.immersivemc.wiki_button"),
                (button -> Minecraft.getInstance().setScreen(
                        new ConfirmLinkScreen((clickedYes) -> {
                            if (clickedYes) {
                                Util.getPlatform().openUri(WIKI_URL);
                            }
                            Minecraft.getInstance().setScreen(this);
                        }, Component.translatable("config.immersivemc.open_wiki_message"),
                                Component.empty(),
                                WIKI_URL,
                                CommonComponents.GUI_CANCEL,
                                true)))
        ));

        this.addRenderableWidget(new Button(
                (this.width - BUTTON_WIDTH) / 2 - (BUTTON_WIDTH / 2) - 8, this.height - 26,
                BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable("gui.done"),
                (button) -> this.onClose()));
        this.addRenderableWidget(new Button(
                (this.width - BUTTON_WIDTH) / 2 + (BUTTON_WIDTH / 2) + 8, this.height - 26,
                BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable("config.immersivemc.reset"),
                (button) -> {
                    ImmersiveMCConfig.resetToDefault();
                    ActiveConfig.loadConfigFromFile();
                }
        ));
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);

        drawCenteredString(stack, this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);

        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        ActiveConfig.loadConfigFromFile();
        Minecraft.getInstance().setScreen(lastScreen);
    }
}
