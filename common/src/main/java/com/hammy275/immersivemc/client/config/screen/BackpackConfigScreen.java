package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.client.immersive.ImmersiveBackpack;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.BackpackMode;
import com.hammy275.immersivemc.common.config.ReachBehindBackpackMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
                    (newModeIndex, newMode) -> ConfigScreen.getClientConfigIfAdjusting().reachBehindBagMode = newMode
            ));

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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderBackpack(graphics.pose());
    }

    protected void renderBackpack(PoseStack stack) {
        stack.pushPose();

        int rgb = ImmersiveBackpack.getBackpackColor();

        float size = 72f;

        stack.translate(this.width * 0.9325, this.height / 2f, 548);
        stack.scale(0.5f, 0.5f, 0.5f);

        long currentTimeMilli = Instant.now().toEpochMilli();
        long millisPerRot = 8000;
        float rot = (((float) (currentTimeMilli % millisPerRot)) / millisPerRot) *
                (2f * (float) Math.PI);
        stack.mulPose(Axis.XN.rotationDegrees(205f));
        stack.mulPose(Axis.YN.rotation(rot));
        stack.translate(0, size * 1.75, 0);
        stack.scale(size, -size, size); // Negative multiplications here to turn it back from being inside-out

        ImmersiveBackpack.getBackpackModel().renderToBuffer(stack,
                Minecraft.getInstance().renderBuffers().bufferSource()
                        .getBuffer(RenderType.entityCutout(ImmersiveBackpack.getBackpackTexture())),
                15728880, OverlayTexture.NO_OVERLAY, rgb);
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();

        stack.popPose();
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
