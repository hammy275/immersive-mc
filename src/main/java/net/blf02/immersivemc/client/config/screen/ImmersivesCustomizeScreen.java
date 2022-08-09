package net.blf02.immersivemc.client.config.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.config.ImmersiveMCConfig;
import net.blf02.immersivemc.common.config.PlacementMode;
import net.minecraft.client.CycleOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ImmersivesCustomizeScreen extends Screen {

    protected final Screen lastScreen;
    protected OptionsList list;

    protected static int BUTTON_WIDTH = 256;
    protected static int BUTTON_HEIGHT = 20;


    public ImmersivesCustomizeScreen(Screen lastScreen) {
        super(new TranslatableComponent("screen.immersivemc.immersives_customize.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();

        this.list = new OptionsList(Minecraft.getInstance(), this.width, this.height,
                32, this.height - 32, 24);

        ScreenUtils.addOption("center_brewing", ImmersiveMCConfig.autoCenterBrewing, this.list);
        ScreenUtils.addOption("center_furnace", ImmersiveMCConfig.autoCenterFurnace, this.list);
        ScreenUtils.addOption("right_click_chest", ImmersiveMCConfig.rightClickChest, this.list);
        ScreenUtils.addOption("show_placement_guide", ImmersiveMCConfig.showPlacementGuide, this.list);

        this.list.addBig(CycleOption.create(
                "config.immersivemc.placement_mode",
                () -> IntStream.rangeClosed(0, 3).boxed().collect(Collectors.toList()),
                (optionIndex) -> new TranslatableComponent("config.immersivemc.placement_mode." + optionIndex),
                (ignored) -> ImmersiveMCConfig.itemPlacementMode.get(),
                (ignored, ignored2, newIndex) -> {
                    ImmersiveMCConfig.itemPlacementMode.set(
                            (ImmersiveMCConfig.itemPlacementMode.get() + newIndex) % PlacementMode.values().length
                    );
                    ImmersiveMCConfig.itemPlacementMode.save();
                    ActiveConfig.loadConfigFromFile();
                }

        ));

        this.addRenderableWidget(this.list);

        this.addRenderableWidget(new Button(
                (this.width - BUTTON_WIDTH) / 2, this.height - 26,
                BUTTON_WIDTH, BUTTON_HEIGHT, new TranslatableComponent("gui.done"),
                (button) -> this.onClose()));
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);

        this.list.render(stack, mouseX, mouseY, partialTicks);

        drawCenteredString(stack, this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);

        super.render(stack, mouseX, mouseY, partialTicks);

        List<FormattedCharSequence> list = OptionsSubScreen.tooltipAt(this.list, mouseX, mouseY);
        if (list != null) {
            this.renderTooltip(stack, list, mouseX, mouseY);
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(lastScreen);
        ActiveConfig.loadConfigFromFile();
    }
}
