package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.client.immersive.AbstractImmersive;
import com.hammy275.immersivemc.client.model.Cube1x1;
import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ImmersiveMCConfig;
import com.hammy275.immersivemc.common.config.PlacementGuideMode;
import com.hammy275.immersivemc.common.util.RGBA;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.time.Instant;

public class ItemGuideCustomizeScreen extends Screen {

    private static final String[] types = new String[]{"item_guide", "item_guide_selected", "ranged_grab"};
    private static final char[] rgba = new char[]{'r', 'g', 'b', 'a'};

    private final Screen lastScreen;

    protected OptionsList list;

    protected static int BUTTON_WIDTH = 128;
    protected static int BUTTON_HEIGHT = 20;

    public ItemGuideCustomizeScreen(Screen lastScreen) {
        super(Component.translatable("screen." + ImmersiveMC.MOD_ID + ".item_guide_customize_screen"));
        this.lastScreen = lastScreen;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float f) {
        this.renderBackground(graphics);

        super.render(graphics, mouseX, mouseY, f);

        graphics.drawCenteredString(this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);

        renderPreview(graphics.pose(), ActiveConfig.FILE.itemGuideColor, 0.25f, false, ActiveConfig.FILE.itemGuideSize);
        renderPreview(graphics.pose(), ActiveConfig.FILE.itemGuideSelectedColor, 0.5f, false, ActiveConfig.FILE.itemGuideSelectedSize);
        // Render square for particle color by using our cube model lol
        renderPreview(graphics.pose(), ActiveConfig.FILE.rangedGrabColor, 0.75f, true, 1.0f);

        if (ScreenUtils.mouseInBox(mouseX, mouseY, this.width * 7 / 8 - 16,
                this.height * 1 / 4 - 16, this.width * 7 / 8 + 16, this.height * 1 / 4 + 16)) {
            graphics.renderTooltip(this.font, Tooltip.splitTooltip(Minecraft.getInstance(), Component.translatable("config.immersivemc.item_guide.desc")),
                    mouseX, mouseY);
        }

        if (ScreenUtils.mouseInBox(mouseX, mouseY, this.width * 7 / 8 - 16,
                this.height * 1 / 2 - 16, this.width * 7 / 8 + 16, this.height * 1 / 2 + 16)) {
            graphics.renderTooltip(this.font, Tooltip.splitTooltip(Minecraft.getInstance(), Component.translatable("config.immersivemc.item_guide_selected.desc")),
                    mouseX, mouseY);
        }

        RGBA color = ActiveConfig.FILE.rangedGrabColor;

        if (ScreenUtils.mouseInBox(mouseX, mouseY, this.width * 7 / 8 - 16,
                this.height * 3 / 4 - 16 + (int) (16f * color.alphaF()), this.width * 7 / 8 + 16, this.height * 3 / 4 + 16 + (int) (16f * color.alphaF()))) {
            graphics.renderTooltip(this.font, Tooltip.splitTooltip(Minecraft.getInstance(), Component.translatable("config.immersivemc.ranged_grab_color.desc")),
                    mouseX, mouseY);
        }
    }

    private void renderPreview(PoseStack stack, RGBA color, float heightMult, boolean renderSquare, double size) {
        stack.pushPose();
        stack.translate(this.width * 0.875, this.height * heightMult, 0);
        stack.scale(0.25f, 0.25f, 0.25f);

        if (!renderSquare) {
            long currentTimeMilli = Instant.now().toEpochMilli();
            long millisPerRot = 8000;
            float rot = (((float) (currentTimeMilli % millisPerRot)) / millisPerRot) *
                    (2f * (float) Math.PI);
            stack.mulPose(Axis.YN.rotation(rot));
        }

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        if (ActiveConfig.FILE.placementGuideMode == PlacementGuideMode.CUBE || renderSquare) {
            stack.translate(0, 64f * size, 0);
            if (renderSquare) {
                stack.translate(0, 64f * color.alphaF(), 0);
                stack.scale(color.alphaF(), color.alphaF(), color.alphaF());
            }
            float alpha = renderSquare ? 1f : color.alphaF();
            ClientRenderSubscriber.cubeModel.render(stack,
                    buffer.getBuffer(RenderType.entityTranslucent(Cube1x1.textureLocation)),
                    color.redF(), color.greenF(), color.blueF(), alpha, 64f * (float)size, AbstractImmersive.maxLight);
        } else if (ActiveConfig.FILE.placementGuideMode == PlacementGuideMode.OUTLINE) {
            LevelRenderer.renderLineBox(stack, buffer.getBuffer(RenderType.LINES),
                    AABB.ofSize(Vec3.ZERO, 128 * size, 128 * size, 128 * size),
                    color.redF(), color.greenF(), color.blueF(), color.alphaF());
        }
        buffer.endBatch();
        stack.popPose();
    }

    @Override
    protected void init() {
        this.list = new OptionsList(Minecraft.getInstance(), this.width * 3 / 4, this.height,
                32, this.height - 32, 24);


        this.list.addBig(
            ScreenUtils.createEnumOption(PlacementGuideMode.class,
                    "config.immersivemc.placement_guide_mode",
                    (guideMode) -> Component.translatable("config.immersivemc.placement_guide_mode." + guideMode.ordinal()),
                    (guideMode -> Component.translatable("config.immersivemc.placement_guide_mode.desc")),
                    () -> ActiveConfig.FILE.placementGuideMode,
                    (newModeIndex, newMode) -> {
                        ImmersiveMCConfig.placementGuideMode.set(newModeIndex);
                        // We don't use loadConfigFromFile here so that other guide values aren't accidentally overwritten
                        ActiveConfig.FILE.placementGuideMode = newMode;
                    }
        ));


        for (int i = 0; i < types.length; i++) {
            RGBA color = i == 0 ? ActiveConfig.FILE.itemGuideColor : i == 1 ? ActiveConfig.FILE.itemGuideSelectedColor : ActiveConfig.FILE.rangedGrabColor;

            if (i == 0) {
                String sizeKey = "config." + ImmersiveMC.MOD_ID + "." + types[i] + "_size";
                this.list.addBig(ScreenUtils.createIntSlider(
                                sizeKey, (value) -> Component.literal(I18n.get(sizeKey) + ": " + String.format("%.02f", (float) value / 100.0f)),
                                0, 100, () -> (int)(ActiveConfig.FILE.itemGuideSize * 100),
                                 (newVal) -> {
                                     ImmersiveMCConfig.itemGuideSize.set(newVal / 100.0d);
                                     ActiveConfig.FILE.itemGuideSize = newVal / 100.0d;
                                 }
                        )
                );
            } else if (i == 1) {
                String sizeKey = "config." + ImmersiveMC.MOD_ID + "." + types[i] + "_size";
                this.list.addBig(ScreenUtils.createIntSlider(
                                sizeKey, (value) -> Component.literal(I18n.get(sizeKey) + ": " + String.format("%.02f", (float) value / 100.0f)),
                                0, 100, () -> (int)(ActiveConfig.FILE.itemGuideSelectedSize * 100),
                                (newVal) -> {
                                    ImmersiveMCConfig.itemGuideSelectedSize.set(newVal / 100.0d);
                                    ActiveConfig.FILE.itemGuideSelectedSize = newVal / 100.0d;
                                }
                        )
                );
            }


            for (char c : rgba) {
                String key = types[i];

                String compKey = "config." + ImmersiveMC.MOD_ID + "." + key + "_" + c;
                int finalI = i;
                this.list.addBig(ScreenUtils.createIntSlider(
                        compKey, (value) ->
                                Component.literal(I18n.get(compKey) + ": " + value),
                        0, 255, () -> color.getColor(c),
                        (newVal) -> {
                            color.setColor(c, newVal);
                            if (finalI == 0) {
                                ImmersiveMCConfig.itemGuideColor.set(ActiveConfig.FILE.itemGuideColor.toLong());
                            } else if (finalI == 1) {
                                ImmersiveMCConfig.itemGuideSelectedColor.set(ActiveConfig.FILE.itemGuideSelectedColor.toLong());
                            } else {
                                ImmersiveMCConfig.rangedGrabColor.set(ActiveConfig.FILE.rangedGrabColor.toLong());
                            }
                        }
                ));
            }
        }

        this.addRenderableWidget(this.list);
        this.addRenderableWidget(ScreenUtils.createDoneButton(
                (this.width - BUTTON_WIDTH) / 2, this.height - 26,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                this
        ));

    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.lastScreen);
    }
}
