package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.client.immersive.ImmersiveBackpack;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.BackpackMode;
import com.hammy275.immersivemc.common.config.ReachBehindBackpackMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.CycleOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.time.Instant;
import java.util.List;

/*
Thanks to https://leo3418.github.io/2021/03/31/forge-mod-config-screen-1-16.html for a guide that was very
helpful in writing this.
*/
public class BackpackConfigScreen extends Screen {

    protected final Screen parentScreen;
    protected OptionsList list;

    protected static int BUTTON_WIDTH = 128;
    protected static int BUTTON_HEIGHT = 20;

    public BackpackConfigScreen(Screen lastScreen) {
        super(new TranslatableComponent("screen." + ImmersiveMC.MOD_ID + ".backpack_config.title"));
        this.parentScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.list = new OptionsList(Minecraft.getInstance(), this.width * 3 / 4, this.height,
                32, this.height - 32, 24);

        initOptionsList();

        this.addRenderableWidget(this.list);

        this.addRenderableWidget(ScreenUtils.createDoneButton(
                (this.width - BUTTON_WIDTH) / 2, this.height - 26,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                this
        ));
    }

    protected void initOptionsList() {

        this.list.addBig(
            ScreenUtils.createEnumOption(BackpackMode.class,
                    "config.immersivemc.backpack_mode",
                    (backpackMode) -> new TranslatableComponent("config.immersivemc.backpack_mode." + backpackMode.ordinal()),
                    (backpackMode) -> new TranslatableComponent("config.immersivemc.backpack_mode." + backpackMode.ordinal() + ".desc"),
                    () -> ConfigScreen.getClientConfigIfAdjusting().bagMode,
                    (newModeIndex, newMode) -> {
                        BackpackMode oldMode = ConfigScreen.getClientConfigIfAdjusting().bagMode;
                        ConfigScreen.getClientConfigIfAdjusting().bagMode = newMode;
                        // Also set ACTIVE mode since that's what getBackpackModel() looks at in renderBackpack()
                        ActiveConfig.activeRaw().bagMode = newMode;
                        if (oldMode.colorable != newMode.colorable) {
                            Minecraft.getInstance().setScreen(new BackpackConfigScreen(parentScreen));
                        }
                    }
        ));

        this.list.addBig(
            ScreenUtils.createEnumOption(ReachBehindBackpackMode.class,
                    "config.immersivemc.reach_behind_backpack_mode",
                    (reachBehindBackpackMode) -> new TranslatableComponent("config.immersivemc.reach_behind_backpack_mode." + reachBehindBackpackMode.ordinal()),
                    (reachBehindBackpackMode) -> new TranslatableComponent("config.immersivemc.reach_behind_backpack_mode." + reachBehindBackpackMode.ordinal() + ".desc"),
                    () -> ConfigScreen.getClientConfigIfAdjusting().reachBehindBagMode,
                    (newModeIndex, newMode) -> ConfigScreen.getClientConfigIfAdjusting().reachBehindBagMode = newMode
            ));

        if (ConfigScreen.getClientConfigIfAdjusting().bagMode.colorable) {
            this.list.addBig(ScreenUtils.createIntSlider(
                    "config.immersivemc.backpack_r",
                    (integer) -> new TextComponent(I18n.get("config.immersivemc.backpack_r") + ": " + getRGB('r')),
                    0, 255,
                    () -> getRGB('r'), (newRVal) -> setRGB(Double.valueOf(newRVal), 'r')
            ));
            this.list.addBig(ScreenUtils.createIntSlider(
                    "config.immersivemc.backpack_g",
                    (integer) -> new TextComponent(I18n.get("config.immersivemc.backpack_g") + ": " + getRGB('g')),
                    0, 255,
                    () -> getRGB('g'), (newRVal) -> setRGB(Double.valueOf(newRVal), 'g')
            ));
            this.list.addBig(ScreenUtils.createIntSlider(
                    "config.immersivemc.backpack_b",
                    (integer) -> new TextComponent(I18n.get("config.immersivemc.backpack_b") + ": " + getRGB('b')),
                    0, 255,
                    () -> getRGB('b'), (newRVal) -> setRGB(Double.valueOf(newRVal), 'b')
            ));
        }
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);

        super.render(stack, mouseX, mouseY, partialTicks);

        drawCenteredString(stack, this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);

        renderBackpack(stack);

        List<FormattedCharSequence> list = OptionsSubScreen.tooltipAt(this.list, mouseX, mouseY);
        if (list != null) {
            this.renderTooltip(stack, list, mouseX, mouseY);
        }

    }

    protected void renderBackpack(PoseStack stack) {
        stack.pushPose();

        Vector3f rgb = ImmersiveBackpack.getBackpackColor();

        float size = 96f;
        stack.translate(this.width * 0.875, this.height / 2f - size * 1.5f, 0);
        stack.scale(-size, -size, -size); // Negative multiplications here to turn it back from being inside-out

        stack.mulPose(Vector3f.XN.rotationDegrees(205));

        long currentTimeMilli = Instant.now().toEpochMilli();
        long millisPerRot = 8000;
        float rot = (((float) (currentTimeMilli % millisPerRot)) / millisPerRot) *
                (2f * (float) Math.PI);
        stack.mulPose(Vector3f.YN.rotation(rot));

        ImmersiveBackpack.getBackpackModel().renderToBuffer(stack,
                Minecraft.getInstance().renderBuffers().bufferSource()
                        .getBuffer(RenderType.entityCutout(ImmersiveBackpack.getBackpackTexture())),
                15728880, OverlayTexture.NO_OVERLAY,
                rgb.x(),rgb.y(), rgb.z(), 1);
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();

        stack.popPose();
    }

    @Override
    public void onClose() {
        ConfigScreen.writeAdjustingConfig();
        Minecraft.getInstance().setScreen(parentScreen);
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

    protected void setRGB(Double newVal, char type) {
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
