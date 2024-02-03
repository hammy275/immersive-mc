package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ImmersiveMCConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

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

        ScreenUtils.addOption("disable_vanilla_guis", ImmersiveMCConfig.disableVanillaGUIs, this.list);
        ScreenUtils.addOption("return_items", ImmersiveMCConfig.returnItems, this.list);
        ScreenUtils.addOption("do_rumble", ImmersiveMCConfig.doRumble, this.list);
        ScreenUtils.addOption("center_brewing", ImmersiveMCConfig.autoCenterBrewing, this.list);
        ScreenUtils.addOption("center_furnace", ImmersiveMCConfig.autoCenterFurnace, this.list);
        ScreenUtils.addOption("right_click_chest", ImmersiveMCConfig.rightClickChest, this.list);
        ScreenUtils.addOption("spin_crafting_output", ImmersiveMCConfig.spinCraftingOutput, this.list);
        ScreenUtils.addOption("pet_any_living", ImmersiveMCConfig.canPetAnyLiving, this.list);
        ScreenUtils.addOption("right_click_in_vr", ImmersiveMCConfig.rightClickInVR, this.list);
        ScreenUtils.addOption("3d_compat", ImmersiveMCConfig.resourcePack3dCompat, this.list);
        ScreenUtils.addOption("crouch_bypass_immersion", ImmersiveMCConfig.crouchBypassImmersion, this.list);

        this.list.addBig(
            ScreenUtils.createEnumOption(PlacementMode.class,
                    "config.immersivemc.placement_mode",
                    (placementMode) -> new TranslatableComponent("config.immersivemc.placement_mode." + placementMode.ordinal()),
                    (placementMode) -> new TranslatableComponent("config.immersivemc.placement_mode.desc",
                            I18n.get("config.immersivemc.placement_mode." + placementMode.ordinal()).toLowerCase()),
                    () -> ActiveConfig.FILE.placementMode,
                    (newModeIndex, newMode) -> {
                        ImmersiveMCConfig.itemPlacementMode.set(newMode.ordinal());
                        ActiveConfig.FILE.loadFromFile();
                    }
            ));

        this.list.addBig(ScreenUtils.createIntSlider(
                "config.immersivemc.ranged_grab_range",
                (val) -> {
                    if (val == -1) {
                        return new TranslatableComponent("config.immersivemc.use_pick_range");
                    }
                    return new TextComponent(I18n.get("config.immersivemc.ranged_grab_range") + ": " + val);
                },
                -1, 12,
                () -> ActiveConfig.FILE.rangedGrabRange, (newVal) -> {
                    ImmersiveMCConfig.rangedGrabRange.set(newVal);
                    ActiveConfig.FILE.loadFromFile();
                }
        ));

        this.addRenderableWidget(this.list);

        this.addRenderableWidget(ScreenUtils.createDoneButton(
                (this.width - BUTTON_WIDTH) / 2, this.height - 26,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                this
        ));
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
    }
}
