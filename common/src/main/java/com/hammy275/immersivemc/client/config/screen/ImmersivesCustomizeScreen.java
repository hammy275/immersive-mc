package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ConfigType;
import com.hammy275.immersivemc.common.config.CrouchMode;
import com.hammy275.immersivemc.common.vr.VRVerify;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class ImmersivesCustomizeScreen extends OptionsSubScreen {

    protected static int BUTTON_WIDTH = 256;
    protected static int BUTTON_HEIGHT = 20;


    public ImmersivesCustomizeScreen(Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, Component.translatable("screen.immersivemc.immersives_customize.title"));
    }

    @Override
    protected void addOptions() {
        ScreenUtils.addOptionIfClient("disable_vanilla_guis", config -> config.disableVanillaInteractionsForSupportedImmersives, (config, newVal) -> config.disableVanillaInteractionsForSupportedImmersives = newVal, this.list);
        ScreenUtils.addOptionIfClient("return_items", config -> config.returnItemsWhenLeavingImmersives, (config, newVal) -> config.returnItemsWhenLeavingImmersives = newVal, this.list);
        ScreenUtils.addOptionIfClient("do_rumble", config -> config.doVRControllerRumble, (config, newVal) -> config.doVRControllerRumble = newVal, this.list);
        ScreenUtils.addOptionIfClient("center_brewing", config -> config.autoCenterBrewingStandImmersive, (config, newVal) -> config.autoCenterBrewingStandImmersive = newVal, this.list);
        ScreenUtils.addOptionIfClient("center_furnace", config -> config.autoCenterFurnaceImmersive, (config, newVal) -> config.autoCenterFurnaceImmersive = newVal, this.list);
        ScreenUtils.addOptionIfClient("right_click_chest", config -> config.rightClickChestInteractions, (config, newVal) -> config.rightClickChestInteractions = newVal, this.list);
        ScreenUtils.addOptionIfClient("spin_crafting_output", config -> config.spinSomeImmersiveOutputs, (config, newVal) -> config.spinSomeImmersiveOutputs = newVal, this.list);
        ScreenUtils.addOption("pet_any_living", config -> config.allowPettingAnythingLiving, (config, newVal) -> {
            config.allowPettingAnythingLiving = newVal;
            if (ConfigScreen.getAdjustingConfigType() == ConfigType.CLIENT && newVal) {
                // If setting to true, also set it to true on server and save.
                // Prevents unintuitive behavior when only adjusting the client config.
                ActiveConfig server = ActiveConfig.getFileConfig(ConfigType.SERVER);
                server.allowPettingAnythingLiving = true;
                server.writeConfigFile(ConfigType.SERVER);
            }
        }, this.list);
        if (VRVerify.clientInVR()) {
            ScreenUtils.addOptionIfClient("right_click_in_vr", config -> config.rightClickImmersiveInteractionsInVR, (config, newVal) -> config.rightClickImmersiveInteractionsInVR = newVal, this.list);
            ScreenUtils.addOptionIfClient("dont_step_up_immersives_in_vr", config -> config.dontAutoStepOnImmersiveBlocksInVR, (config, newVal) -> config.dontAutoStepOnImmersiveBlocksInVR = newVal, this.list);
        }
        ScreenUtils.addOptionIfClient("3d_compat", config -> config.compatFor3dResourcePacks, (config, newVal) -> config.compatFor3dResourcePacks = newVal, this.list);

        if (ConfigScreen.getAdjustingConfigType() == ConfigType.CLIENT) {
            this.list.addBig(ScreenUtils.createEnumOption(CrouchMode.class, "config.immersivemc.crouch_mode",
                    mode -> Component.translatable("config.immersivemc.crouch_mode." + mode.ordinal()),
                    mode -> Component.translatable("config.immersivemc.crouch_mode." + mode.ordinal() + ".desc"),
                    () -> ConfigScreen.getClientConfigIfAdjusting().crouchMode,
                    (newModeIndex, newMode) -> ConfigScreen.getClientConfigIfAdjusting().crouchMode = newMode));
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
            this.list.addBig(ScreenUtils.createIntSlider(
                    "config.immersivemc.text_scale",
                    // Need to do the format below before passing to Minecraft as Minecraft doesn't seem to handle
                    // forcing a certain decimal length.
                    val -> Component.translatable("config.immersivemc.text_scale_val", String.format("%.2f", val / 20f)),
                    10, 40,
                    () -> (int) (ConfigScreen.getClientConfigIfAdjusting().textScale * 20),
                    newVal -> ConfigScreen.getClientConfigIfAdjusting().textScale = newVal / 20f
            ));
        }

        if (VRVerify.clientInVR()) {
            ScreenUtils.addOptionIfClient("grab_beacon", config -> config.useGrabBeaconInVR, (config, newVal) -> config.useGrabBeaconInVR = newVal, this.list);
            ScreenUtils.addOptionIfClient("grind_grindstone", config -> config.useGrindMotionGrindstoneInVR, (config, newVal) -> config.useGrindMotionGrindstoneInVR = newVal, this.list);
        }
    }

    @Override
    public void onClose() {
        ConfigScreen.writeAdjustingConfig();
        super.onClose();
    }
}
