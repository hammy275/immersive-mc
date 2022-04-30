package net.blf02.immersivemc.client.config.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.config.ImmersiveMCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.util.text.TranslationTextComponent;

public class ImmersivesConfigScreen extends Screen {

    protected final Screen lastScreen;
    protected OptionsRowList list;
    protected boolean notInWorld;

    protected static int BUTTON_WIDTH = 256;
    protected static int BUTTON_HEIGHT = 20;

    public ImmersivesConfigScreen(Screen screen) {
        super(new TranslationTextComponent("screen.immersivemc.immersives_config.title"));
        this.lastScreen = screen;
    }

    @Override
    protected void init() {
        super.init();
        this.notInWorld = Minecraft.getInstance().level == null && Minecraft.getInstance().player == null;
        if (this.notInWorld) {
            initNotInWorld();
        }
    }

    protected void initNotInWorld() {
        this.list = new OptionsRowList(Minecraft.getInstance(), this.width, this.height,
                32, this.height - 32, 24);

        initOptionsList();

        this.children.add(this.list);

        this.addButton(new Button(
                (this.width - BUTTON_WIDTH) / 2, this.height - 26,
                BUTTON_WIDTH, BUTTON_HEIGHT, new TranslationTextComponent("gui.done"),
                (button) -> this.onClose()));
    }

    protected void initOptionsList() {
        ScreenUtils.addOption("anvil", ImmersiveMCConfig.useAnvilImmersion, this.list);
        ScreenUtils.addOption("backpack_button", ImmersiveMCConfig.useBackpack, this.list);
        ScreenUtils.addOption("brewing", ImmersiveMCConfig.useBrewingImmersion, this.list);
        ScreenUtils.addOption("button", ImmersiveMCConfig.useButton, this.list);
        ScreenUtils.addOption("campfire", ImmersiveMCConfig.useCampfireImmersion, this.list);
        ScreenUtils.addOption("chest", ImmersiveMCConfig.useChestImmersion, this.list);
        ScreenUtils.addOption("crafting", ImmersiveMCConfig.useCraftingImmersion, this.list);
        ScreenUtils.addOption("enchanting_table", ImmersiveMCConfig.useETableImmersion, this.list);
        ScreenUtils.addOption("furnace", ImmersiveMCConfig.useFurnaceImmersion, this.list);
        ScreenUtils.addOption("jukebox", ImmersiveMCConfig.useJukeboxImmersion, this.list);
        ScreenUtils.addOption("lever", ImmersiveMCConfig.useLever, this.list);

        ScreenUtils.addOption("ranged_grab", ImmersiveMCConfig.useRangedGrab, this.list);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);

        if (this.notInWorld) {
            this.list.render(stack, mouseX, mouseY, partialTicks);

            drawCenteredString(stack, this.font, this.title.getString(),
                    this.width / 2, 8, 0xFFFFFF);
            drawCenteredString(stack, this.font, new TranslationTextComponent("screen.immersivemc.immersives_config.subtitle"),
                    this.width / 2, 8 + this.font.lineHeight, 0xFFFFFF);
        } else {
            // Not actually sure if you can get here, but I'm playing things safe by catching if you're in a world while
            // you try to adjust this
            drawCenteredString(stack, this.font, new TranslationTextComponent("screen.immersivemc.immersives_config.inworld"),
                    this.width / 2, this.height / 2, 0xFFFFFF);
        }

        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(lastScreen);
        ActiveConfig.loadConfigFromFile();
    }
}
