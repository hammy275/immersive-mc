package net.blf02.immersivemc.client.config.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.blf02.immersivemc.ImmersiveMC;
import net.blf02.immersivemc.client.immersive.BackpackImmersive;
import net.blf02.immersivemc.client.model.BackpackModel;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.config.ImmersiveMCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.SliderPercentageOption;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.time.Instant;
import java.util.List;

/*
Thanks to https://leo3418.github.io/2021/03/31/forge-mod-config-screen-1-16.html for a guide that was very
helpful in writing this.
*/
public class BackpackConfigScreen extends Screen {

    protected final Screen parentScreen;
    protected OptionsRowList list;

    protected static int BUTTON_WIDTH = 128;
    protected static int BUTTON_HEIGHT = 20;

    public BackpackConfigScreen(Screen lastScreen) {
        super(new TranslationTextComponent("screen." + ImmersiveMC.MOD_ID + ".backpack_config.title"));
        this.parentScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.list = new OptionsRowList(Minecraft.getInstance(), this.width / 2, this.height / 2,
                32, this.height - 32, 24);

        initOptionsList();

        this.children.add(this.list);

        this.addButton(new Button(
                (this.width - BUTTON_WIDTH) / 2, this.height - 26,
                BUTTON_WIDTH, BUTTON_HEIGHT, new TranslationTextComponent("gui.done"),
                (button) -> this.onClose()));
    }

    protected void initOptionsList() {
        this.list.addBig(ScreenUtils.createOption("left_handed_backpack", ImmersiveMCConfig.leftHandedBackpack));
        this.list.addBig(ScreenUtils.createOption("low_detail_backpack", ImmersiveMCConfig.useLowDetailBackpack));
        this.list.addBig(new SliderPercentageOption(
                "config.immersivemc.backpack_r", 0, 255, 1,
                (ignored) -> (double) getRGB('r'), (ignored, newVal) -> setRGB(newVal, 'r'),
                (ignored, ignored2) ->
                        new StringTextComponent(I18n.get("config.immersivemc.backpack_r") + ": " + getRGB('r')
                        )));
        this.list.addBig(new SliderPercentageOption(
                "config.immersivemc.backpack_g", 0, 255, 1,
                (ignored) -> (double) getRGB('g'), (ignored, newVal) -> setRGB(newVal, 'g'),
                (ignored, ignored2) ->
                        new StringTextComponent(I18n.get("config.immersivemc.backpack_g") + ": " + getRGB('g')
                        )));
        this.list.addBig(new SliderPercentageOption(
                "config.immersivemc.backpack_b", 0, 255, 1,
                (ignored) -> (double) getRGB('b'), (ignored, newVal) -> setRGB(newVal, 'b'),
                (ignored, ignored2) ->
                        new StringTextComponent(I18n.get("config.immersivemc.backpack_b") + ": " + getRGB('b')
                        )));

    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);

        this.list.render(stack, mouseX, mouseY, partialTicks);

        drawCenteredString(stack, this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);

        super.render(stack, mouseX, mouseY, partialTicks);

        renderBackpack(stack);

        List<IReorderingProcessor> list = SettingsScreen.tooltipAt(this.list, mouseX, mouseY);
        if (list != null) {
            this.renderTooltip(stack, list, mouseX, mouseY);
        }

    }

    protected void renderBackpack(MatrixStack stack) {
        stack.pushPose();

        Vector3f rgb = new Vector3f(ActiveConfig.backpackColor >> 16, ActiveConfig.backpackColor >> 8 & 255,
                ActiveConfig.backpackColor & 255);
        rgb.mul(1f/255f);

        float size = 128f;
        stack.translate(this.width * 0.75, this.height / 2f - size * 1.5f, 0);
        stack.scale(size, size, size);

        stack.mulPose(Vector3f.XN.rotationDegrees(45));

        long currentTimeMilli = Instant.now().toEpochMilli();
        long millisPerRot = 8000;
        float rot = (((float) (currentTimeMilli % millisPerRot)) / millisPerRot) *
                (2f * (float) Math.PI);
        stack.mulPose(Vector3f.YN.rotation(rot));

        BackpackImmersive.getBackpackModel().renderToBuffer(stack,
                Minecraft.getInstance().renderBuffers().bufferSource()
                        .getBuffer(RenderType.entityCutout(BackpackModel.textureLocation)),
                15728880, OverlayTexture.NO_OVERLAY,
                rgb.x(),rgb.y(), rgb.z(), 1);
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();

        stack.popPose();
    }

    @Override
    public void onClose() {
        ImmersiveMCConfig.backpackColor.set(ActiveConfig.backpackColor);
        ImmersiveMCConfig.backpackColor.save();
        Minecraft.getInstance().setScreen(parentScreen);
        ActiveConfig.loadConfigFromFile();
    }

    protected int getRGB(char type) {
        if (type == 'r') {
            return ActiveConfig.backpackColor >> 16;
        } else if (type == 'g') {
            return ActiveConfig.backpackColor >> 8 & 255;
        } else {
            return ActiveConfig.backpackColor & 255;
        }
    }

    protected void setRGB(Double newVal, char type) {
        Vector3i rgb = new Vector3i(getRGB('r'), getRGB('g'), getRGB('b'));
        System.out.print(newVal + "\r");
        if (type == 'r') {
            rgb = new Vector3i(newVal, rgb.getY(), rgb.getZ());
        } else if (type == 'g') {
            rgb = new Vector3i(rgb.getX(), newVal, rgb.getZ());
        } else {
            rgb = new Vector3i(rgb.getX(), rgb.getY(), newVal);
        }
        ActiveConfig.backpackColor = (rgb.getX() << 16) + (rgb.getY() << 8) + (rgb.getZ());
    }
}
