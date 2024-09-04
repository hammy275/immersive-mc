package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.common.config.ConfigType;
import com.hammy275.immersivemc.common.config.PlacementMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

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

        this.list = new OptionsList(Minecraft.getInstance(),
                this.width, this.height - 64, 32, 24);

        ScreenUtils.addOptionIfClient("disable_vanilla_guis", config -> config.disableVanillaGUIs, (config, newVal) -> config.disableOutsideVR = newVal, this.list);
        ScreenUtils.addOptionIfClient("return_items", config -> config.returnItems, (config, newVal) -> config.returnItems = newVal, this.list);
        ScreenUtils.addOptionIfClient("do_rumble", config -> config.doRumble, (config, newVal) -> config.doRumble = newVal, this.list);
        ScreenUtils.addOptionIfClient("center_brewing", config -> config.autoCenterBrewing, (config, newVal) -> config.autoCenterBrewing = newVal, this.list);
        ScreenUtils.addOptionIfClient("center_furnace", config -> config.autoCenterFurnace, (config, newVal) -> config.autoCenterFurnace = newVal, this.list);
        ScreenUtils.addOptionIfClient("right_click_chest", config -> config.rightClickChest, (config, newVal) -> config.rightClickChest = newVal, this.list);
        ScreenUtils.addOptionIfClient("spin_crafting_output", config -> config.spinCraftingOutput, (config, newVal) -> config.spinCraftingOutput = newVal, this.list);
        ScreenUtils.addOption("pet_any_living", config -> config.canPetAnyLiving, (config, newVal) -> config.canPetAnyLiving = newVal, this.list);
        ScreenUtils.addOptionIfClient("right_click_in_vr", config -> config.rightClickInVR, (config, newVal) -> config.rightClickInVR = newVal, this.list);
        ScreenUtils.addOptionIfClient("3d_compat", config -> config.resourcePack3dCompat, (config, newVal) -> config.resourcePack3dCompat = newVal, this.list);
        ScreenUtils.addOptionIfClient("crouch_bypass_immersion", config -> config.crouchBypassImmersion, (config, newVal) -> config.crouchBypassImmersion = newVal, this.list);

        if (ConfigScreen.getAdjustingConfigType() == ConfigType.CLIENT) {
            this.list.addBig(
                    ScreenUtils.createEnumOption(PlacementMode.class,
                            "config.immersivemc.placement_mode",
                            (placementMode) -> Component.translatable("config.immersivemc.placement_mode." + placementMode.ordinal()),
                            (placementMode) -> Component.translatable("config.immersivemc.placement_mode.desc",
                                    I18n.get("config.immersivemc.placement_mode." + placementMode.ordinal()).toLowerCase()),
                            () -> ConfigScreen.getClientConfigIfAdjusting().placementMode,
                            (newModeIndex, newMode) -> ConfigScreen.getClientConfigIfAdjusting().placementMode = newMode

                    ));

            this.list.addBig(ScreenUtils.createIntSlider(
                    "config.immersivemc.ranged_grab_range",
                    (val) -> {
                        if (val == -1) {
                            return Component.translatable("config.immersivemc.use_pick_range");
                        }
                        return Component.literal(I18n.get("config.immersivemc.ranged_grab_range") + ": " + val);
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);

        super.render(graphics, mouseX, mouseY, partialTicks);

        graphics.drawCenteredString(this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);

    }

    @Override
    public void onClose() {
        ConfigScreen.writeAdjustingConfig();
        Minecraft.getInstance().setScreen(lastScreen);
    }
}
