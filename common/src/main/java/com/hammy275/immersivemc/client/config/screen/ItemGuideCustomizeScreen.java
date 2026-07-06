package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.model.Cube1x1;
import com.hammy275.immersivemc.client.model.CustomGuiRendererState;
import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ItemGuideColorData;
import com.hammy275.immersivemc.common.config.ItemGuidePreset;
import com.hammy275.immersivemc.common.config.PlacementGuideMode;
import com.hammy275.immersivemc.common.util.RGBA;
import com.hammy275.immersivemc.mixin.GuiGraphicsExtractorAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ItemGuideCustomizeScreen extends OptionsSubScreen {

    private static final String[] types = new String[]{"item_guide_custom", "item_guide_selected_custom", "ranged_grab_custom"};
    private static final char[] rgba = new char[]{'r', 'g', 'b', 'a'};

    protected static int BUTTON_WIDTH = 128;
    protected static int BUTTON_HEIGHT = 20;

    public ItemGuideCustomizeScreen(Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, Component.translatable("screen." + ImmersiveMC.MOD_ID + ".item_guide_customize_screen"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        ClientRenderSubscriber.setRenderColors();
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        renderPreview(graphics, ClientRenderSubscriber.itemGuideColor(), 0.25f, false, ConfigScreen.getClientConfigIfAdjusting().itemGuideSize);
        renderPreview(graphics, ClientRenderSubscriber.itemGuideSelectedColor(), 0.5f, false, ConfigScreen.getClientConfigIfAdjusting().itemGuideSelectedSize);
        // Render square for particle color by using our cube model lol
        renderPreview(graphics, ClientRenderSubscriber.rangedGrabColor(), 0.75f, true, 1.0f);

        if (ScreenUtils.mouseInBox(mouseX, mouseY, this.width * 19 / 20 - 16,
                this.height * 250 / 1000 - 16, this.width * 19 / 20 + 16, this.height * 250 / 1000 + 16)) {
            graphics.tooltip(this.font, List.of(ClientTooltipComponent.create(Component.translatable("config.immersivemc.item_guide.desc").getVisualOrderText())),
                    mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
        }

        if (ScreenUtils.mouseInBox(mouseX, mouseY, this.width * 19 / 20 - 16,
                this.height * 500 / 1000 - 16, this.width * 19 / 20 + 16, this.height * 500 / 1000 + 16)) {
            graphics.tooltip(this.font, List.of(ClientTooltipComponent.create(Component.translatable("config.immersivemc.item_guide_selected.desc").getVisualOrderText())),
                    mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
        }

        if (ScreenUtils.mouseInBox(mouseX, mouseY, this.width * 19 / 20 - 16,
                this.height * 750 / 1000 - 16, this.width * 19 / 20 + 16, this.height * 750 / 1000 + 16)) {
            graphics.tooltip(this.font, List.of(ClientTooltipComponent.create(Component.translatable("config.immersivemc.ranged_grab_color.desc").getVisualOrderText())),
                    mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
        }
    }

    private void renderPreview(GuiGraphicsExtractor graphics, RGBA color, float heightMult, boolean renderSquare, double size) {
        GuiRenderState guiRenderState = ((GuiGraphicsExtractorAccessor) graphics).immersiveMC$getGuiRenderState();
        ScreenRectangle peek = Platform.CLIENT.peekScissorStack(graphics);

        guiRenderState.addPicturesInPictureState(new CustomGuiRendererState(
                this.width * 0.9, this.width, this.height * heightMult - 64f, this.height * heightMult + 64f, 0.5f, peek,
                (stack, bufferSource) -> {
                    float renderSize = (float) size / 2f; // Cut size in half on 1.21.11+ to match older versions for this parameter
                    if (!renderSquare) {
                        long currentTimeMilli = Instant.now().toEpochMilli();
                        long millisPerRot = 8000;
                        float rot = (((float) (currentTimeMilli % millisPerRot)) / millisPerRot) *
                                (2f * (float) Math.PI);
                        stack.mulPose(Axis.YN.rotation(rot));
                    } else {
                        renderSize = color.alphaF() / 2f;
                    }

                    if (ConfigScreen.getClientConfigIfAdjusting().placementGuideMode == PlacementGuideMode.CUBE || renderSquare) {
                        RGBA renderColor = renderSquare ? new RGBA(color.toLong() | 0xFF000000L) : color;
                        stack.pushPose();
                        stack.scale(1024f * renderSize, 1024f * renderSize, 1024f * renderSize);
                        bufferSource.submitModel(ClientRenderSubscriber.cubeModel, null, stack,
                                RenderTypes.entityTranslucent(Cube1x1.textureLocation), ClientUtil.maxLight, OverlayTexture.NO_OVERLAY,
                                (int) renderColor.toLong(), null, 0x00000000, null);
                        stack.popPose();
                    } else if (ConfigScreen.getClientConfigIfAdjusting().placementGuideMode == PlacementGuideMode.OUTLINE) {
                        stack.scale(128f * renderSize, 128f * renderSize, 128f * renderSize);
                        bufferSource.submitCustomGeometry(stack, RenderTypes.lines(), (pose, vertexConsumer) -> cube(pose, vertexConsumer, (int) color.toLong()));
                    }
                }
        ));
    }

    private void cube(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        for (float y = -0.5f; y <= 0.5f; y++) {
            for (float xz = -0.5f; xz <= 0.5f; xz++) {
                line(pose, buffer, xz, y, -0.5f, xz, y, 0.5f, color);
                line(pose, buffer, -0.5f, y, xz, 0.5f, y, xz, color);
            }
        }
        // TODO: These below lines flash for whatever reason. Fix this!
        for (float x = -0.5f; x <= 0.5f; x++) {
            for (float z = -0.5f; z <= 0.5f; z++) {
                line(pose, buffer, x, -0.5f, z, x, 0.5f, z, color);
            }
        }
    }

    private void line(PoseStack.Pose pose, VertexConsumer buffer, float startX, float startY, float startZ, float endX, float endY, float endZ, int color) {
        float diffX = endX - startX;
        float diffY = endY - startY;
        float diffZ = endZ - startZ;
        buffer.addVertex(pose, startX, startY, startZ)
                .setNormal(pose, diffX, diffY, diffZ)
                .setColor(color)
                .setLineWidth(2.5F);
        buffer.addVertex(pose, endX, endY, endZ)
                .setNormal(pose, diffX, diffY, diffZ)
                .setColor(color)
                .setLineWidth(2.5F);
    }

    @Override
    protected void addOptions() {
        this.list.addBig(
                ScreenUtils.createEnumOption(PlacementGuideMode.class,
                        "config.immersivemc.placement_guide_mode",
                        (guideMode) -> Component.translatable("config.immersivemc.placement_guide_mode." + guideMode.ordinal()),
                        (guideMode -> Component.translatable("config.immersivemc.placement_guide_mode.desc")),
                        () -> ConfigScreen.getClientConfigIfAdjusting().placementGuideMode,
                        (newModeIndex, newMode) -> {
                            ConfigScreen.getClientConfigIfAdjusting().placementGuideMode = newMode;
                        }
                ));

        this.list.addBig(ScreenUtils.createIntSlider(
                        "config.immersivemc.item_guide_size", (value) -> Component.literal(I18n.get("config.immersivemc.item_guide_size") + ": " + String.format("%.02f", (float) value / 100.0f)),
                        0, 100, () -> (int) (ConfigScreen.getClientConfigIfAdjusting().itemGuideSize * 100),
                        (newVal) -> ConfigScreen.getClientConfigIfAdjusting().itemGuideSize = newVal / 100.0d
                )
        );
        this.list.addBig(ScreenUtils.createIntSlider(
                        "config.immersivemc.item_guide_selected_size", (value) -> Component.literal(I18n.get("config.immersivemc.item_guide_selected_size") + ": " + String.format("%.02f", (float) value / 100.0f)),
                        0, 100, () -> (int) (ConfigScreen.getClientConfigIfAdjusting().itemGuideSelectedSize * 100),
                        (newVal) -> ConfigScreen.getClientConfigIfAdjusting().itemGuideSelectedSize = newVal / 100.0d
                )
        );
        if (ConfigScreen.getClientConfigIfAdjusting().itemGuidePreset != ItemGuidePreset.CUSTOM) {
            this.list.addBig(ScreenUtils.createIntSlider(
                            "config.immersivemc.ranged_grab_a", (value) -> Component.literal(I18n.get("config.immersivemc.ranged_grab_a") + ": " + value),
                            0, 255, () -> ConfigScreen.getClientConfigIfAdjusting().colorPresetRangedGrabSize,
                            newVal -> ConfigScreen.getClientConfigIfAdjusting().colorPresetRangedGrabSize = newVal
                    )
            );
        }

        this.list.addBig(
                ScreenUtils.createEnumOption(ItemGuidePreset.class,
                        "config.immersivemc.item_guide_preset",
                        preset -> Component.translatable("config.immersivemc.item_guide_preset." + preset.ordinal()),
                        preset -> Component.literal(I18n.get("config.immersivemc.item_guide_preset.desc") + "\n\n" + I18n.get("config.immersivemc.item_guide_preset." + preset.ordinal() + ".desc")),
                        () -> ConfigScreen.getClientConfigIfAdjusting().itemGuidePreset,
                        (newPresetIndex, newPreset) -> {
                            ConfigScreen.getClientConfigIfAdjusting().itemGuidePreset = newPreset;
                            this.resetList();
                        }
                ));

        if (ConfigScreen.getClientConfigIfAdjusting().itemGuidePreset == ItemGuidePreset.PRIDE_FLAG) {
            this.list.addBig(
                    ScreenUtils.createEnumOption(ItemGuidePreset.PrideFlag.class,
                            "config.immersivemc.item_guide_preset.pride_flag_option",
                            flag -> Component.translatable("config.immersivemc.item_guide_preset.pride_flag_option." + flag.name().toLowerCase()),
                            flag -> Component.empty(),
                            () -> ConfigScreen.getClientConfigIfAdjusting().itemGuidePrideFlag,
                            (newFlagIndex, newFlag) -> ConfigScreen.getClientConfigIfAdjusting().itemGuidePrideFlag = newFlag
                    ));
        }

        if (ConfigScreen.getClientConfigIfAdjusting().itemGuidePreset.isCustomizablePreset()) {
            this.list.addBig(ScreenUtils.createIntSlider(
                    "config.immersivemc.item_guide_a", (value) -> Component.literal(I18n.get("config.immersivemc.item_guide_a") + ": " + value),
                            0, 255, () -> ConfigScreen.getClientConfigIfAdjusting().colorPresetAlpha,
                            newVal -> ConfigScreen.getClientConfigIfAdjusting().colorPresetAlpha = newVal
                    )
            );
            this.list.addBig(ScreenUtils.createIntSlider(
                    "config.immersivemc.item_guide_selected_a", (value) -> Component.literal(I18n.get("config.immersivemc.item_guide_selected_a") + ": " + value),
                            0, 255, () -> ConfigScreen.getClientConfigIfAdjusting().colorPresetSelectedAlpha,
                            newVal -> ConfigScreen.getClientConfigIfAdjusting().colorPresetSelectedAlpha = newVal
                    )
            );
        }

        if (ConfigScreen.getClientConfigIfAdjusting().itemGuidePreset.showTransitionConfig()) {
            this.list.addBig(
                    ScreenUtils.createIntSlider(
                            "config.immersivemc.item_guide_transition_time",
                            value -> Component.translatable("config.immersivemc.item_guide_transition_time_value", String.format("%.02f", (float) value / 1000.0f)),
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
                this.list.addBig(ScreenUtils.createIntSlider(
                        "config.immersivemc.num_custom_colors." + key,
                        value -> Component.literal(I18n.get("config.immersivemc.num_custom_colors." + key) + ": " + value),
                        1, 10,
                        colors::size,
                        newVal -> {
                            List<RGBA> newColors = new ArrayList<>(colors);
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
                            resetList();
                        }
                        ));
                addColorOptions(colors, key);
            }
        }
    }

    private void resetList() {
        double scrollAmount = this.list.scrollAmount();
        this.list.replaceEntries(List.of());
        addOptions();
        this.list.setScrollAmount(Math.min(scrollAmount, this.list.maxScrollAmount()));
    }

    private void addColorOptions(List<RGBA> colors, String key) {
        for (int i = 0; i < colors.size(); i++) {
            RGBA color = colors.get(i);
            for (char c : rgba) {
                String compKey = "config." + ImmersiveMC.MOD_ID + "." + key + "." + c;
                int readableColorNum = i + 1;
                this.list.addBig(ScreenUtils.createIntSlider(
                        compKey,
                        value -> Component.translatable(compKey, String.valueOf(readableColorNum), String.valueOf(value)),
                        0, 255, () -> color.getColor(c),
                        (newVal) -> color.setColor(c, newVal)
                ));
            }
        }
    }

    @Override
    public void onClose() {
        ConfigScreen.writeAdjustingConfig();
        super.onClose();
    }
}
