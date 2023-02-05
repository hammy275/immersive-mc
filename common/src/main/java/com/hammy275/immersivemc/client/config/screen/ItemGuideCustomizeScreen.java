package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.client.immersive.AbstractImmersive;
import com.hammy275.immersivemc.client.model.Cube1x1;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ImmersiveMCConfig;
import com.hammy275.immersivemc.common.config.PlacementGuideMode;
import com.hammy275.immersivemc.common.util.RGBA;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.CycleOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ItemGuideCustomizeScreen extends Screen {

    private final AABB renderHitbox = AABB.ofSize(Vec3.ZERO, 128, 128, 128);

    private static final String[] types = new String[]{"item_guide", "item_guide_selected", "ranged_grab"};
    private static final char[] rgba = new char[]{'r', 'g', 'b', 'a'};

    private final Screen lastScreen;

    protected OptionsList list;

    protected static int BUTTON_WIDTH = 128;
    protected static int BUTTON_HEIGHT = 20;

    public ItemGuideCustomizeScreen(Screen lastScreen) {
        super(new TranslatableComponent("screen." + ImmersiveMC.MOD_ID + ".item_guide_customize_screen"));
        this.lastScreen = lastScreen;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float f) {
        this.renderBackground(stack);

        super.render(stack, mouseX, mouseY, f);

        drawCenteredString(stack, this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);

        renderPreview(stack, ActiveConfig.itemGuideColor, 0.25f, false);
        renderPreview(stack, ActiveConfig.itemGuideSelectedColor, 0.5f, false);
        // Render square for particle color by using our cube model lol
        renderPreview(stack, ActiveConfig.rangedGrabColor, 0.75f, true);

        if (ScreenUtils.mouseInBox(mouseX, mouseY, this.width * 3 / 4 - 16,
                this.height * 1 / 4 - 16, this.width * 3 / 4 + 16, this.height * 1 / 4 + 16)) {
            renderTooltip(stack, Tooltip.splitTooltip(Minecraft.getInstance(), Component.translatable("config.immersivemc.item_guide.desc")),
                    mouseX, mouseY);
        }

        if (ScreenUtils.mouseInBox(mouseX, mouseY, this.width * 3 / 4 - 16,
                this.height * 1 / 2 - 16, this.width * 3 / 4 + 16, this.height * 1 / 2 + 16)) {
            renderTooltip(stack, Tooltip.splitTooltip(Minecraft.getInstance(), Component.translatable("config.immersivemc.item_guide_selected.desc")),
                    mouseX, mouseY);
        }

        if (ScreenUtils.mouseInBox(mouseX, mouseY, this.width * 3 / 4 - 16,
                this.height * 3 / 4 - 16, this.width * 3 / 4 + 16, this.height * 3 / 4 + 16)) {
            renderTooltip(stack, Tooltip.splitTooltip(Minecraft.getInstance(), Component.translatable("config.immersivemc.ranged_grab_color.desc")),
                    mouseX, mouseY);
        }
    }

    private void renderPreview(PoseStack stack, RGBA color, float heightMult, boolean renderSquare) {
        stack.pushPose();
        stack.translate(this.width * 0.75, this.height * heightMult, 0);
        stack.scale(0.25f, 0.25f, 0.25f);

        if (!renderSquare) {
            long currentTimeMilli = Instant.now().toEpochMilli();
            long millisPerRot = 8000;
            float rot = (((float) (currentTimeMilli % millisPerRot)) / millisPerRot) *
                    (2f * (float) Math.PI);
            stack.mulPose(Vector3f.YN.rotation(rot));
        }

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        if (ActiveConfig.placementGuideMode == PlacementGuideMode.CUBE || renderSquare) {
            stack.translate(0, 64f, 0);
            if (renderSquare) {
                stack.translate(0, 64f * color.alphaF(), 0);
                stack.scale(color.alphaF(), color.alphaF(), color.alphaF());
            }
            float alpha = renderSquare ? 1f : color.alphaF();
            AbstractImmersive.cubeModel.render(stack,
                    buffer.getBuffer(RenderType.entityTranslucent(Cube1x1.textureLocation)),
                    color.redF(), color.greenF(), color.blueF(), alpha, 64f);
        } else if (ActiveConfig.placementGuideMode == PlacementGuideMode.OUTLINE) {
            LevelRenderer.renderLineBox(stack, buffer.getBuffer(RenderType.LINES),
                    renderHitbox,
                    color.redF(), color.greenF(), color.blueF(), color.alphaF());
        }
        buffer.endBatch();
        stack.popPose();
    }

    @Override
    protected void init() {
        this.list = new OptionsList(Minecraft.getInstance(), this.width / 2, this.height / 2,
                32, this.height - 32, 24);


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

        for (int i = 0; i < types.length; i++) {
            RGBA color = i == 0 ? ActiveConfig.itemGuideColor : i == 1 ? ActiveConfig.itemGuideSelectedColor : ActiveConfig.rangedGrabColor;
            for (char c : rgba) {
                String key = types[i];

                String compKey = "config." + ImmersiveMC.MOD_ID + "." + key + "_" + c;
                this.list.addBig(ScreenUtils.createIntSlider(
                        compKey, (value) ->
                                new TextComponent(I18n.get(compKey) + ": " + value),
                        0, 255, color.getColor(c),
                        (newVal) -> color.setColor(c, newVal)
                ));
            }
        }

        this.addRenderableWidget(this.list);
        this.addRenderableWidget(new Button(
                (this.width - BUTTON_WIDTH) / 2, this.height - 26,
                BUTTON_WIDTH, BUTTON_HEIGHT, new TranslatableComponent("gui.done"),
                (button) -> this.onClose()));

    }

    @Override
    public void onClose() {
        ImmersiveMCConfig.itemGuideColor.set(ActiveConfig.itemGuideColor.toLong());
        ImmersiveMCConfig.itemGuideSelectedColor.set(ActiveConfig.itemGuideSelectedColor.toLong());
        ImmersiveMCConfig.rangedGrabColor.set(ActiveConfig.rangedGrabColor.toLong());
        Minecraft.getInstance().setScreen(this.lastScreen);
        ActiveConfig.loadConfigFromFile();
    }
}
