package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ImmersiveMCConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hammy275.immersivemc.common.config.PlacementGuideMode;
import net.minecraft.client.CycleOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ProgressOption;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
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
        ScreenUtils.addOption("spin_crafting_output", ImmersiveMCConfig.spinCraftingOutput, this.list);
        ScreenUtils.addOption("pet_any_living", ImmersiveMCConfig.canPetAnyLiving, this.list);
        if (Minecraft.getInstance().level == null || ActiveConfig.serverCopy != null) {
            ScreenUtils.addOption("right_click_in_vr", ImmersiveMCConfig.rightClickInVR, this.list);
        }
        ScreenUtils.addOption("3d_compat", ImmersiveMCConfig.resourcePack3dCompat, this.list);
        ScreenUtils.addOption("crouch_bypass_immersion", ImmersiveMCConfig.crouchBypassImmersion, this.list);

        this.list.addBig(CycleOption.create(
                "config.immersivemc.placement_mode",
                () -> IntStream.rangeClosed(0, 3).boxed().collect(Collectors.toList()),
                (optionIndex) -> new TranslatableComponent("config.immersivemc.placement_mode." + optionIndex),
                (ignored) -> ImmersiveMCConfig.itemPlacementMode.get(),
                (ignored, ignored2, newIndex) -> {
                    ImmersiveMCConfig.itemPlacementMode.set(
                            newIndex
                    );
                    ImmersiveMCConfig.itemPlacementMode.save();
                    ActiveConfig.loadConfigFromFile();
                }

        ).setTooltip(
                (minecraft) -> (optionIndex) -> minecraft.font.split(
                        new TranslatableComponent("config.immersivemc.placement_mode.desc",
                                I18n.get("config.immersivemc.placement_mode." + optionIndex).toLowerCase()),
                        200)
        ));

        this.list.addBig(CycleOption.create(
                "config.immersivemc.placement_guide_mode",
                () -> IntStream.rangeClosed(0, PlacementGuideMode.values().length - 1).boxed().collect(Collectors.toList()),
                (optionIndex) -> new TranslatableComponent("config.immersivemc.placement_guide_mode." + optionIndex),
                (ignored) -> ImmersiveMCConfig.placementGuideMode.get(),
                (ignored, ignored2, newIndex) -> {
                    ImmersiveMCConfig.placementGuideMode.set(
                            newIndex
                    );
                    ImmersiveMCConfig.placementGuideMode.save();
                    ActiveConfig.loadConfigFromFile();
                }
        ).setTooltip(
                (minecraft) -> (optionIndex) -> minecraft.font.split(
                        new TranslatableComponent("config.immersivemc.placement_guide_mode.desc"
                        ), 200
                )
                )
        );


        if (Minecraft.getInstance().level == null || ActiveConfig.serverCopy != null) {
            this.list.addBig(new ProgressOption(
                    "config.immersivemc.ranged_grab_range", -1, 12, 1,
                    (ignored) -> (double) ActiveConfig.rangedGrabRange, (ignored, newVal) -> {
                        ImmersiveMCConfig.rangedGrabRange.set((int) (double) newVal);
            },
                    (ignored, ignored2) ->
                    {
                        if (ImmersiveMCConfig.rangedGrabRange.get() == -1) {
                            return new TranslatableComponent("config.immersivemc.use_pick_range");
                        }
                        return new TextComponent(I18n.get("config.immersivemc.ranged_grab_range") + ": " + ImmersiveMCConfig.rangedGrabRange.get());
                    }));
        }


        this.addRenderableWidget(this.list);

        this.addRenderableWidget(new Button(
                (this.width - BUTTON_WIDTH) / 2, this.height - 26,
                BUTTON_WIDTH, BUTTON_HEIGHT, new TranslatableComponent("gui.done"),
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
