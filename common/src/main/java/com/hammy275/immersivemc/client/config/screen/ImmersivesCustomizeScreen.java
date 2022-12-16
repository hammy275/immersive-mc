package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ImmersiveMCConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ImmersivesCustomizeScreen extends Screen {

    protected final Screen lastScreen;
    protected OptionsList list;

    protected static int BUTTON_WIDTH = 256;
    protected static int BUTTON_HEIGHT = 20;


    public ImmersivesCustomizeScreen(Screen lastScreen) {
        super(Component.translatable("screen.immersivemc.immersives_customize.title"));
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
        ScreenUtils.addOption("spin_crafting_output", ImmersiveMCConfig.spinCraftingOutput, this.list);
        ScreenUtils.addOption("pet_any_living", ImmersiveMCConfig.canPetAnyLiving, this.list);

        this.list.addBig(
                new OptionInstance<>(
                        "config.immersivemc.placement_mode",
                        mc -> placementMode -> Minecraft.getInstance().font.split(
                                Component.translatable("config.immersivemc.placement_mode.desc",
                                        I18n.get("config.immersivemc.placement_mode." + placementMode.ordinal()).toLowerCase()), 200),
                        (component, placementMode) -> Component.translatable("config.immersivemc.placement_mode." + placementMode.ordinal()),
                        new OptionInstance.LazyEnum<>(
                                () -> Arrays.asList(PlacementMode.values()),
                                Optional::of,
                                null

                        ),
                        ActiveConfig.placementMode,
                        (newMode) -> {
                            ImmersiveMCConfig.itemPlacementMode.set(newMode.ordinal());
                            ImmersiveMCConfig.itemPlacementMode.save();
                            ActiveConfig.loadConfigFromFile();
                        }
                )
        );


        if (Minecraft.getInstance().level == null || ActiveConfig.serverCopy != null) {
            this.list.addBig(new OptionInstance<>(
                    "config.immersivemc.ranged_grab_range", OptionInstance.noTooltip(),
                    (component, val) -> {
                        if (val == -1) {
                            return Component.translatable("config.immersivemc.use_pick_range");
                        }
                        return Component.literal(I18n.get("config.immersivemc.ranged_grab_range") + ": " + val);
                    },
                    new OptionInstance.IntRange(-1, 12),
                    ActiveConfig.rangedGrabRange, (newVal) -> {
                ImmersiveMCConfig.rangedGrabRange.set(newVal);
            }
            ));
        }


        this.addRenderableWidget(this.list);

        this.addRenderableWidget(new Button(
                (this.width - BUTTON_WIDTH) / 2, this.height - 26,
                BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable("gui.done"),
                (button) -> this.onClose()));
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);

        super.render(stack, mouseX, mouseY, partialTicks);

        drawCenteredString(stack, this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);

        List<FormattedCharSequence> list = OptionsSubScreen.tooltipAt(this.list, mouseX, mouseY);
        if (list != null) {
            this.renderTooltip(stack, list, mouseX, mouseY);
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(lastScreen);
        ImmersiveMCConfig.rangedGrabRange.save();
        ActiveConfig.loadConfigFromFile();
        if (Minecraft.getInstance().level != null) {
            ActiveConfig.reloadAfterServer();
        } else {
            ActiveConfig.loadConfigFromFile();
        }
    }
}
