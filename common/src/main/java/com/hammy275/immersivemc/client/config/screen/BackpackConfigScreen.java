package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.client.immersive.ImmersiveBackpack;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.BackpackMode;
import com.hammy275.immersivemc.common.config.ImmersiveMCConfig;
import com.hammy275.immersivemc.common.config.ReachBehindBackpackMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import org.joml.Vector3f;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

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
        super(Component.translatable("screen." + ImmersiveMC.MOD_ID + ".backpack_config.title"));
        this.parentScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.list = new OptionsList(Minecraft.getInstance(), this.width * 3 / 4, this.height,
                32, this.height - 32, 24);

        initOptionsList();

        this.addRenderableWidget(this.list);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                        (button) -> this.onClose())
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .pos((this.width - BUTTON_WIDTH) / 2, this.height - 26)
                .build());
    }

    protected void initOptionsList() {
        this.list.addBig(
                new OptionInstance<>(
                        "config.immersivemc.backpack_mode",
                        backpackMode -> Tooltip.create(
                                Component.translatable("config.immersivemc.backpack_mode." + backpackMode.ordinal() + ".desc")),
                        (component, backpackMode) -> Component.translatable("config.immersivemc.backpack_mode." + backpackMode.ordinal()),
                        new OptionInstance.LazyEnum<>(
                                () -> Arrays.asList(BackpackMode.values()),
                                Optional::of,
                                null
                        ),
                        ActiveConfig.FILE.backpackMode,
                        (newMode) -> {
                            BackpackMode oldMode = ActiveConfig.FILE.backpackMode;
                            ImmersiveMCConfig.backpackMode.set(newMode.ordinal());
                            ActiveConfig.FILE.backpackMode = newMode;
                            // Also set ACTIVE mode since that's what getBackpackModel() looks at in renderBackpack()
                            ActiveConfig.active().backpackMode = newMode;
                            if (oldMode.colorable != newMode.colorable) {
                                Minecraft.getInstance().setScreen(new BackpackConfigScreen(parentScreen));
                            }
                        }
                )
        );
        this.list.addBig(
                new OptionInstance<>(
                        "config.immersivemc.reach_behind_backpack_mode",
                        reachBehindBackpackMode -> Tooltip.create(
                                Component.translatable("config.immersivemc.reach_behind_backpack_mode." + reachBehindBackpackMode.ordinal() + ".desc")),
                        (component, reachBehindBackpackMode) -> Component.translatable("config.immersivemc.reach_behind_backpack_mode." + reachBehindBackpackMode.ordinal()),
                        new OptionInstance.LazyEnum<>(
                                () -> Arrays.asList(ReachBehindBackpackMode.values()),
                                Optional::of,
                                null
                        ),
                        ActiveConfig.FILE.reachBehindBackpackMode,
                        (newMode) -> {
                            ImmersiveMCConfig.reachBehindBackpackMode.set(newMode.ordinal());
                            ActiveConfig.FILE.reachBehindBackpackMode = newMode;
                        }
                )
        );
        if (ActiveConfig.FILE.backpackMode.colorable) {
            this.list.addBig(ScreenUtils.createIntSlider(
                    "config.immersivemc.backpack_r",
                    (integer) -> Component.literal(I18n.get("config.immersivemc.backpack_r") + ": " + getRGB('r')),
                    0, 255,
                    getRGB('r'), (newRVal) -> setRGB(newRVal, 'r')
            ));
            this.list.addBig(ScreenUtils.createIntSlider(
                    "config.immersivemc.backpack_g",
                    (integer) -> Component.literal(I18n.get("config.immersivemc.backpack_g") + ": " + getRGB('g')),
                    0, 255,
                    getRGB('g'), (newRVal) -> setRGB(newRVal, 'g')
            ));
            this.list.addBig(ScreenUtils.createIntSlider(
                    "config.immersivemc.backpack_b",
                    (integer) -> Component.literal(I18n.get("config.immersivemc.backpack_b") + ": " + getRGB('b')),
                    0, 255,
                    getRGB('b'), (newRVal) -> setRGB(newRVal, 'b')
            ));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);

        super.render(graphics, mouseX, mouseY, partialTicks);

        graphics.drawCenteredString(this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);

        renderBackpack(graphics.pose());

    }

    protected void renderBackpack(PoseStack stack) {
        stack.pushPose();

        Vector3f rgb = ImmersiveBackpack.getBackpackColor();

        float size = 96f;
        stack.translate(this.width * 0.875, this.height / 2f - size * 1.5f, 0);
        stack.scale(-size, -size, -size); // Negative multiplications here to turn it back from being inside-out

        stack.mulPose(Axis.XN.rotationDegrees(205));

        long currentTimeMilli = Instant.now().toEpochMilli();
        long millisPerRot = 8000;
        float rot = (((float) (currentTimeMilli % millisPerRot)) / millisPerRot) *
                (2f * (float) Math.PI);
        stack.mulPose(Axis.YN.rotation(rot));

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
        Minecraft.getInstance().setScreen(parentScreen);
    }

    protected int getRGB(char type) {
        if (type == 'r') {
            return ActiveConfig.FILE.backpackColor >> 16;
        } else if (type == 'g') {
            return ActiveConfig.FILE.backpackColor >> 8 & 255;
        } else {
            return ActiveConfig.FILE.backpackColor & 255;
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
        ImmersiveMCConfig.backpackColor.set(newColor);
        ActiveConfig.FILE.backpackColor = newColor;
        // Also set ACTIVE mode since that's what getBackpackModel() looks at in renderBackpack()
        ActiveConfig.active().backpackColor = newColor;
    }
}
