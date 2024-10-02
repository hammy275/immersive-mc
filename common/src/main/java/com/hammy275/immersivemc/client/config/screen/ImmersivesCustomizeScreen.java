package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.common.config.ConfigType;
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

        ScreenUtils.addOptionIfClient("disable_vanilla_guis", config -> config.disableVanillaInteractionsForSupportedImmersives, (config, newVal) -> config.disableVanillaInteractionsForSupportedImmersives = newVal, this.list);
        ScreenUtils.addOptionIfClient("return_items", config -> config.returnItemsWhenLeavingImmersives, (config, newVal) -> config.returnItemsWhenLeavingImmersives = newVal, this.list);
        ScreenUtils.addOptionIfClient("do_rumble", config -> config.doVRControllerRumble, (config, newVal) -> config.doVRControllerRumble = newVal, this.list);
        ScreenUtils.addOptionIfClient("center_brewing", config -> config.autoCenterBrewingStandImmersive, (config, newVal) -> config.autoCenterBrewingStandImmersive = newVal, this.list);
        ScreenUtils.addOptionIfClient("center_furnace", config -> config.autoCenterFurnaceImmersive, (config, newVal) -> config.autoCenterFurnaceImmersive = newVal, this.list);
        ScreenUtils.addOptionIfClient("right_click_chest", config -> config.rightClickChestInteractions, (config, newVal) -> config.rightClickChestInteractions = newVal, this.list);
        ScreenUtils.addOptionIfClient("spin_crafting_output", config -> config.spinSomeImmersiveOutputs, (config, newVal) -> config.spinSomeImmersiveOutputs = newVal, this.list);
        ScreenUtils.addOption("pet_any_living", config -> config.allowPettingAnythingLiving, (config, newVal) -> config.allowPettingAnythingLiving = newVal, this.list);
        ScreenUtils.addOptionIfClient("right_click_in_vr", config -> config.rightClickImmersiveInteractionsInVR, (config, newVal) -> config.rightClickImmersiveInteractionsInVR = newVal, this.list);
        ScreenUtils.addOptionIfClient("3d_compat", config -> config.compatFor3dResourcePacks, (config, newVal) -> config.compatFor3dResourcePacks = newVal, this.list);
        ScreenUtils.addOptionIfClient("crouch_bypass_immersion", config -> config.crouchingBypassesImmersives, (config, newVal) -> config.crouchingBypassesImmersives = newVal, this.list);

        if (ConfigScreen.getAdjustingConfigType() == ConfigType.CLIENT) {
            this.list.addBig(
                    ScreenUtils.createEnumOption(PlacementMode.class,
                            "config.immersivemc.placement_mode",
                            (placementMode) -> new TranslatableComponent("config.immersivemc.placement_mode." + placementMode.ordinal()),
                            (placementMode) -> new TranslatableComponent("config.immersivemc.placement_mode.desc",
                                    I18n.get("config.immersivemc.placement_mode." + placementMode.ordinal()).toLowerCase()),
                            () -> ConfigScreen.getClientConfigIfAdjusting().placementMode,
                            (newModeIndex, newMode) -> ConfigScreen.getClientConfigIfAdjusting().placementMode = newMode

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
                    () -> ConfigScreen.getClientConfigIfAdjusting().rangedGrabRange, (newVal) -> ConfigScreen.getClientConfigIfAdjusting().rangedGrabRange = newVal
            ));
        }

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
        ConfigScreen.writeAdjustingConfig();
        Minecraft.getInstance().setScreen(lastScreen);
    }
}
