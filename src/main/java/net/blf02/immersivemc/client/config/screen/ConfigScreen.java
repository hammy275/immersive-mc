package net.blf02.immersivemc.client.config.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;

public class ConfigScreen extends Screen {

    protected final Screen lastScreen;

    protected static int BUTTON_WIDTH = 256;
    protected static int BUTTON_HEIGHT = 20;

    public ConfigScreen(Screen screen) {
        super(new TranslationTextComponent("screen.immersivemc.config.title"));
        this.lastScreen = screen;
    }

    @Override
    protected void init() {
        ActiveConfig.loadConfigFromFile(); // Load config so we're working with our current values when changing them
        super.init();

        this.addButton(new Button(
                (this.width - BUTTON_WIDTH) / 2, this.height / 2 - BUTTON_HEIGHT,
                BUTTON_WIDTH, BUTTON_HEIGHT, new TranslationTextComponent("config.immersivemc.backpack"),
                (button) -> Minecraft.getInstance().setScreen(new BackpackConfigScreen(this))
        ));

        this.addButton(new Button(
                (this.width - BUTTON_WIDTH) / 2, this.height / 2 + BUTTON_HEIGHT,
                BUTTON_WIDTH, BUTTON_HEIGHT, new TranslationTextComponent("config.immersivemc.immersives"),
                (button) -> Minecraft.getInstance().setScreen(new ImmersivesConfigScreen(this))
        ));

        this.addButton(new Button(
                (this.width - BUTTON_WIDTH) / 2, this.height - 26,
                BUTTON_WIDTH, BUTTON_HEIGHT, new TranslationTextComponent("gui.done"),
                (button) -> this.onClose()));
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);

        drawCenteredString(stack, this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);

        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(lastScreen);
    }
}
