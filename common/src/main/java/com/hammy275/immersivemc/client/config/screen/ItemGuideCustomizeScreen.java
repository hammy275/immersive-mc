package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.model.Cube1x1;
import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ItemGuideColorData;
import com.hammy275.immersivemc.common.config.ItemGuidePreset;
import com.hammy275.immersivemc.common.config.PlacementGuideMode;
import com.hammy275.immersivemc.common.util.RGBA;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.time.Instant;
import java.util.List;

public class ItemGuideCustomizeScreen extends Screen {

    private static final String[] types = new String[]{"item_guide_custom", "item_guide_selected_custom", "ranged_grab_custom"};
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
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        ClientRenderSubscriber.setRenderColors();
        this.renderBackground(stack);

        super.render(stack, mouseX, mouseY, partialTicks);
        drawCenteredString(stack, this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);

        renderPreview(stack, ClientRenderSubscriber.itemGuideColor(), 0.25f, false, ConfigScreen.getClientConfigIfAdjusting().itemGuideSize);
        renderPreview(stack, ClientRenderSubscriber.itemGuideSelectedColor(), 0.5f, false, ConfigScreen.getClientConfigIfAdjusting().itemGuideSelectedSize);
        // Render square for particle color by using our cube model lol
        renderPreview(stack, ClientRenderSubscriber.rangedGrabColor(), 0.75f, true, 1.0f);

        if (ScreenUtils.mouseInBox(mouseX, mouseY, this.width * 19 / 20 - 16,
                this.height * 250 / 1000 - 16, this.width * 19 / 20 + 16, this.height * 250 / 1000 + 16)) {
            renderTooltip(stack, Minecraft.getInstance().font.split(new TranslatableComponent("config.immersivemc.item_guide.desc"), 170),
                    mouseX, mouseY);
        }

        if (ScreenUtils.mouseInBox(mouseX, mouseY, this.width * 19 / 20 - 16,
                this.height * 500 / 1000 - 16, this.width * 19 / 20 + 16, this.height * 500 / 1000 + 16)) {
            renderTooltip(stack, Minecraft.getInstance().font.split(new TranslatableComponent("config.immersivemc.item_guide_selected.desc"), 170),
                    mouseX, mouseY);
        }

        if (ScreenUtils.mouseInBox(mouseX, mouseY, this.width * 19 / 20 - 16,
                this.height * 750 / 1000 - 16, this.width * 19 / 20 + 16, this.height * 750 / 1000 + 16)) {
            renderTooltip(stack, Minecraft.getInstance().font.split(new TranslatableComponent("config.immersivemc.ranged_grab_color.desc"), 170),
                    mouseX, mouseY);
        }
    }
    private void renderPreview(PoseStack stack, RGBA color, float heightMult, boolean renderSquare, double size) {
        stack.pushPose();
        stack.translate(this.width * 0.95, this.height * heightMult - 16f, 0);
        stack.scale(0.225f, 0.225f, 0.225f);

        if (!renderSquare) {
            long currentTimeMilli = Instant.now().toEpochMilli();
            long millisPerRot = 8000;
            float rot = (((float) (currentTimeMilli % millisPerRot)) / millisPerRot) *
                    (2f * (float) Math.PI);
            stack.mulPose(Vector3f.YN.rotation(rot));
        }

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        if (ConfigScreen.getClientConfigIfAdjusting().placementGuideMode == PlacementGuideMode.CUBE || renderSquare) {
            if (renderSquare) {
                stack.translate(0, 64f * color.alphaF(), 0);
                stack.scale(color.alphaF(), color.alphaF(), color.alphaF());
            } else {
                stack.translate(0, 64f * size, 0);
            }
            float alpha = renderSquare ? 1f : color.alphaF();
            ClientRenderSubscriber.cubeModel.render(stack,
                    buffer.getBuffer(RenderType.entityTranslucent(Cube1x1.textureLocation)),
                    color.redF(), color.greenF(), color.blueF(), alpha, 64f * (float)size, ClientUtil.maxLight);
        } else if (ConfigScreen.getClientConfigIfAdjusting().placementGuideMode == PlacementGuideMode.OUTLINE) {
            LevelRenderer.renderLineBox(stack, buffer.getBuffer(RenderType.LINES),
                    AABB.ofSize(Vec3.ZERO, 128 * size, 128 * size, 128 * size),
                    color.redF(), color.greenF(), color.blueF(), color.alphaF());
        }
        buffer.endBatch();
        stack.popPose();
    }

    @Override
    protected void init() {
        this.list = new OptionsList(Minecraft.getInstance(), this.width, this.height,
                32, this.height - 32, 24);


        this.list.addBig(
            ScreenUtils.createEnumOption(PlacementGuideMode.class,
                    "config.immersivemc.placement_guide_mode",
                    (guideMode) -> new TranslatableComponent("config.immersivemc.placement_guide_mode." + guideMode.ordinal()),
                    (guideMode -> new TranslatableComponent("config.immersivemc.placement_guide_mode.desc")),
                    () -> ConfigScreen.getClientConfigIfAdjusting().placementGuideMode,
                    (newModeIndex, newMode) -> {
                        ConfigScreen.getClientConfigIfAdjusting().placementGuideMode = newMode;
                    }
        ));

        this.list.addBig(ScreenUtils.createIntSlider(
                        "config.immersivemc.item_guide_size", (value) -> new TextComponent(I18n.get("config.immersivemc.item_guide_size") + ": " + String.format("%.02f", (float) value / 100.0f)),
                        0, 100, () -> (int) (ConfigScreen.getClientConfigIfAdjusting().itemGuideSize * 100),
                        (newVal) -> ConfigScreen.getClientConfigIfAdjusting().itemGuideSize = newVal / 100.0d
                )
        );
        this.list.addBig(ScreenUtils.createIntSlider(
                        "config.immersivemc.item_guide_selected_size", (value) -> new TextComponent(I18n.get("config.immersivemc.item_guide_selected_size") + ": " + String.format("%.02f", (float) value / 100.0f)),
                        0, 100, () -> (int) (ConfigScreen.getClientConfigIfAdjusting().itemGuideSelectedSize * 100),
                        (newVal) -> ConfigScreen.getClientConfigIfAdjusting().itemGuideSelectedSize = newVal / 100.0d
                )
        );
        if (ConfigScreen.getClientConfigIfAdjusting().itemGuidePreset != ItemGuidePreset.CUSTOM) {
            this.list.addBig(ScreenUtils.createIntSlider(
                            "config.immersivemc.ranged_grab_a", (value) -> new TextComponent(I18n.get("config.immersivemc.ranged_grab_a") + ": " + value),
                            0, 255, () -> ConfigScreen.getClientConfigIfAdjusting().colorPresetRangedGrabSize,
                            newVal -> ConfigScreen.getClientConfigIfAdjusting().colorPresetRangedGrabSize = newVal
                    )
            );
        }
        
        this.list.addBig(
                ScreenUtils.createEnumOption(ItemGuidePreset.class,
                        "config.immersivemc.item_guide_preset",
                        preset -> new TranslatableComponent("config.immersivemc.item_guide_preset." + preset.ordinal()),
                        preset -> new TextComponent(I18n.get("config.immersivemc.item_guide_preset.desc") + "\n\n" + I18n.get("config.immersivemc.item_guide_preset." + preset.ordinal() + ".desc")),
                        () -> ConfigScreen.getClientConfigIfAdjusting().itemGuidePreset,
                        (newPresetIndex, newPreset) -> {
                            ConfigScreen.getClientConfigIfAdjusting().itemGuidePreset = newPreset;
                            Minecraft.getInstance().setScreen(new ItemGuideCustomizeScreen(this.lastScreen));
                        }
                ));

        if (ConfigScreen.getClientConfigIfAdjusting().itemGuidePreset == ItemGuidePreset.PRIDE_FLAG) {
            this.list.addBig(
                    ScreenUtils.createEnumOption(ItemGuidePreset.PrideFlag.class,
                            "config.immersivemc.item_guide_preset.pride_flag_option",
                            flag -> new TranslatableComponent("config.immersivemc.item_guide_preset.pride_flag_option." + flag.name().toLowerCase()),
                            flag -> TextComponent.EMPTY,
                            () -> ConfigScreen.getClientConfigIfAdjusting().itemGuidePrideFlag,
                            (newFlagIndex, newFlag) -> ConfigScreen.getClientConfigIfAdjusting().itemGuidePrideFlag = newFlag
                    ));
        }

        if (ConfigScreen.getClientConfigIfAdjusting().itemGuidePreset.isCustomizablePreset()) {
            this.list.addBig(ScreenUtils.createIntSlider(
                    "config.immersivemc.item_guide_a", (value) -> new TextComponent(I18n.get("config.immersivemc.item_guide_a") + ": " + value),
                            0, 255, () -> ConfigScreen.getClientConfigIfAdjusting().colorPresetAlpha,
                            newVal -> ConfigScreen.getClientConfigIfAdjusting().colorPresetAlpha = newVal
                    )
            );
            this.list.addBig(ScreenUtils.createIntSlider(
                    "config.immersivemc.item_guide_selected_a", (value) -> new TextComponent(I18n.get("config.immersivemc.item_guide_selected_a") + ": " + value),
                            0, 255, () -> ConfigScreen.getClientConfigIfAdjusting().colorPresetSelectedAlpha,
                            newVal -> ConfigScreen.getClientConfigIfAdjusting().colorPresetSelectedAlpha = newVal
                    )
            );
        }

        if (ConfigScreen.getClientConfigIfAdjusting().itemGuidePreset.showTransitionConfig()) {
            this.list.addBig(
                    ScreenUtils.createIntSlider(
                            "config.immersivemc.item_guide_transition_time",
                            value -> new TranslatableComponent("config.immersivemc.item_guide_transition_time_value", String.format("%.02f", (float) value / 1000.0f)),
                            500, 60000,
                            () -> ConfigScreen.getClientConfigIfAdjusting().multiColorPresetTransitionTimeMS,
                            newVal -> {
                                ConfigScreen.getClientConfigIfAdjusting().multiColorPresetTransitionTimeMS = newVal;
                                ConfigScreen.getClientConfigIfAdjusting().itemGuideCustomColorData = ConfigScreen.getClientConfigIfAdjusting().itemGuideCustomColorData.withChangedTransitionTime(newVal);
                            }
                    ));
        }

        if (ConfigScreen.getClientConfigIfAdjusting().itemGuidePreset == ItemGuidePreset.CUSTOM) {

            ItemGuideColorData colorData = ActiveConfig.FILE_CLIENT.itemGuideCustomColorData;
            for (String key : types) {
                List<RGBA> colors = key.equals(types[0]) ? colorData.colors().get() : key.equals(types[1]) ? colorData.selectedColors().get() : colorData.rangedGrabColors().get();
                // TODO: Don't just reset the screen when number of custom colors is adjusted
                this.list.addBig(ScreenUtils.createIntSlider(
                        "config.immersivemc.num_custom_colors." + key,
                        value -> new TextComponent(I18n.get("config.immersivemc.num_custom_colors." + key) + ": " + value),
                        1, 10,
                        colors::size,
                        newVal -> {
                            List<RGBA> newColors = colors;
                            while (newVal > newColors.size()) {
                                newColors.add(RGBA.random());
                            }
                            if (newVal < newColors.size()) {
                                newColors = newColors.subList(0, newVal);
                            }
                            List<RGBA> newColorsFinal = newColors;
                            if (key.equals(types[0])) {
                                ActiveConfig.FILE_CLIENT.itemGuideCustomColorData = new ItemGuideColorData(() -> newColorsFinal,
                                        colorData.selectedColors(), colorData.rangedGrabColors(), colorData.transitionTimeMS());
                            } else if (key.equals(types[1])) {
                                ActiveConfig.FILE_CLIENT.itemGuideCustomColorData = new ItemGuideColorData(colorData.colors(),
                                        () -> newColorsFinal, colorData.rangedGrabColors(), colorData.transitionTimeMS());
                            } else {
                                ActiveConfig.FILE_CLIENT.itemGuideCustomColorData = new ItemGuideColorData(colorData.colors(),
                                        colorData.selectedColors(), () -> newColorsFinal, colorData.transitionTimeMS());
                            }
                            // Prevents different cycles from getting out of sync when returning to other modes
                            ClientRenderSubscriber.resetCycleProgresses();
                            Minecraft.getInstance().setScreen(new ItemGuideCustomizeScreen(this.lastScreen));
                        }
                        ));
                addColorOptions(colors, key);
            }
        }

        this.addRenderableWidget(this.list);
        this.addRenderableWidget(ScreenUtils.createDoneButton(
                (this.width - BUTTON_WIDTH) / 2, this.height - 26,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                this
        ));
    }

    private void addColorOptions(List<RGBA> colors, String key) {
        for (int i = 0; i < colors.size(); i++) {
            RGBA color = colors.get(i);
            for (char c : rgba) {
                String compKey = "config." + ImmersiveMC.MOD_ID + "." + key + "." + c;
                int readableColorNum = i + 1;
                this.list.addBig(ScreenUtils.createIntSlider(
                        compKey,
                        value -> new TranslatableComponent(compKey, String.valueOf(readableColorNum), String.valueOf(value)),
                        0, 255, () -> color.getColor(c),
                        (newVal) -> color.setColor(c, newVal)
                ));
            }
        }
    }

    @Override
    public void onClose() {
        ConfigScreen.writeAdjustingConfig();
        Minecraft.getInstance().setScreen(this.lastScreen);
    }
}
