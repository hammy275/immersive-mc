package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.PlatformClient;
import com.hammy275.immersivemc.client.immersive.ImmersiveBackpack;
import com.hammy275.immersivemc.client.model.CustomGuiRendererState;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.BackpackMode;
import com.hammy275.immersivemc.common.config.ClientActiveConfig;
import com.hammy275.immersivemc.common.config.ReachBehindBackpackMode;
import com.hammy275.immersivemc.mixin.GuiGraphicsAccessor;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;

import java.time.Instant;

/*
Thanks to https://leo3418.github.io/2021/03/31/forge-mod-config-screen-1-16.html for a guide that was very
helpful in writing this.
*/
public class BackpackConfigScreen extends OptionsSubScreen {

    protected static int BUTTON_WIDTH = 128;
    protected static int BUTTON_HEIGHT = 20;

    public BackpackConfigScreen(Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, Component.translatable("screen." + ImmersiveMC.MOD_ID + ".backpack_config.title"));
    }

    @Override
    protected void addOptions() {
        this.list.addBig(
            ScreenUtils.createEnumOption(BackpackMode.class,
                    "config.immersivemc.backpack_mode",
                    (backpackMode) -> Component.translatable("config.immersivemc.backpack_mode." + backpackMode.ordinal()),
                    (backpackMode) -> Component.translatable("config.immersivemc.backpack_mode." + backpackMode.ordinal() + ".desc"),
                    () -> ConfigScreen.getClientConfigIfAdjusting().bagMode,
                    (newModeIndex, newMode) -> {
                        BackpackMode oldMode = ConfigScreen.getClientConfigIfAdjusting().bagMode;
                        ConfigScreen.getClientConfigIfAdjusting().bagMode = newMode;
                        // Also set ACTIVE mode since that's what getBackpackModel() looks at in renderBackpack()
                        ActiveConfig.activeRaw().bagMode = newMode;
                        if (oldMode.colorable != newMode.colorable) {
                            Minecraft.getInstance().setScreen(new BackpackConfigScreen(lastScreen));
                        }
                    }
        ));

        this.list.addBig(
            ScreenUtils.createEnumOption(ReachBehindBackpackMode.class,
                    "config.immersivemc.reach_behind_backpack_mode",
                    (reachBehindBackpackMode) -> Component.translatable("config.immersivemc.reach_behind_backpack_mode." + reachBehindBackpackMode.ordinal()),
                    (reachBehindBackpackMode) -> Component.translatable("config.immersivemc.reach_behind_backpack_mode." + reachBehindBackpackMode.ordinal() + ".desc"),
                    () -> ConfigScreen.getClientConfigIfAdjusting().reachBehindBagMode,
                    (newModeIndex, newMode) -> {
                        ConfigScreen.getClientConfigIfAdjusting().reachBehindBagMode = newMode;
                        Minecraft.getInstance().setScreen(new BackpackConfigScreen(lastScreen));
                    }
            ));

        this.list.addBig(ScreenUtils.createOption("swap_bag_hand",
                config -> ((ClientActiveConfig) config).swapBagHand,
                (config, val) -> ((ClientActiveConfig) config).swapBagHand = val));

        if (ConfigScreen.getClientConfigIfAdjusting().reachBehindBagMode != ReachBehindBackpackMode.NONE) {
            this.list.addBig(ScreenUtils.createOption(
                    "trigger_hit_for_bag",
                    config -> ((ClientActiveConfig) config).requireTriggerForBagOpen,
                    (config, newVal) -> ((ClientActiveConfig) config).requireTriggerForBagOpen = newVal
            ));
        }
        if (ConfigScreen.getClientConfigIfAdjusting().bagMode.colorable) {
            this.list.addBig(ScreenUtils.createIntSlider(
                    "config.immersivemc.backpack_r",
                    (integer) -> Component.literal(I18n.get("config.immersivemc.backpack_r") + ": " + getRGB('r')),
                    0, 255,
                    () -> getRGB('r'), (newRVal) -> setRGB(newRVal, 'r')
            ));
            this.list.addBig(ScreenUtils.createIntSlider(
                    "config.immersivemc.backpack_g",
                    (integer) -> Component.literal(I18n.get("config.immersivemc.backpack_g") + ": " + getRGB('g')),
                    0, 255,
                    () -> getRGB('g'), (newRVal) -> setRGB(newRVal, 'g')
            ));
            this.list.addBig(ScreenUtils.createIntSlider(
                    "config.immersivemc.backpack_b",
                    (integer) -> Component.literal(I18n.get("config.immersivemc.backpack_b") + ": " + getRGB('b')),
                    0, 255,
                    () -> getRGB('b'), (newRVal) -> setRGB(newRVal, 'b')
            ));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        GuiRenderState guiRenderState = ((GuiGraphicsAccessor) graphics).immersiveMC$getGuiRenderState();
        ScreenRectangle peek = PlatformClient.peekScissorStack(graphics);
        guiRenderState.submitPicturesInPictureState(new CustomGuiRendererState(
                this.width * 0.85, this.width, 0, this.height, 50f, peek,
                (poseStack, bufferSource) -> {
                    poseStack.translate(0, -1.5f, 0); // Translate down (becoming up after XN rotation) since bag model's origin is the top
                    long currentTimeMilli = Instant.now().toEpochMilli();
                    long millisPerRot = 8000;
                    float rot = (((float) (currentTimeMilli % millisPerRot)) / millisPerRot) *
                            (2f * (float) Math.PI);
                    poseStack.mulPose(Axis.XN.rotationDegrees(335f));
                    poseStack.mulPose(Axis.YP.rotation(rot));
                    ImmersiveBackpack.getBackpackModel().renderToBuffer(poseStack,
                            bufferSource.getBuffer(RenderType.entityCutout(ImmersiveBackpack.getBackpackTexture())),
                            15728880, OverlayTexture.NO_OVERLAY, ImmersiveBackpack.getBackpackColor());
                }
        ));
    }

    @Override
    public void onClose() {
        ConfigScreen.writeAdjustingConfig();
        super.onClose();
    }

    protected int getRGB(char type) {
        if (type == 'r') {
            return ConfigScreen.getClientConfigIfAdjusting().bagColor >> 16;
        } else if (type == 'g') {
            return ConfigScreen.getClientConfigIfAdjusting().bagColor >> 8 & 255;
        } else {
            return ConfigScreen.getClientConfigIfAdjusting().bagColor & 255;
        }
    }

    protected void setRGB(Integer newVal, char type) {
        Vec3i rgb = new Vec3i(getRGB('r'), getRGB('g'), getRGB('b'));
        if (type == 'r') {
            rgb = new Vec3i(newVal, rgb.getY(), rgb.getZ());
        } else if (type == 'g') {
            rgb = new Vec3i(rgb.getX(), newVal, rgb.getZ());
        } else {
            rgb = new Vec3i(rgb.getX(), rgb.getY(), newVal);
        }
        int newColor = (rgb.getX() << 16) + (rgb.getY() << 8) + (rgb.getZ());
        ConfigScreen.getClientConfigIfAdjusting().bagColor = newColor;
        // Also set ACTIVE mode since that's what getBackpackModel() looks at in renderBackpack()
        ActiveConfig.activeRaw().bagColor = newColor;
    }
}
