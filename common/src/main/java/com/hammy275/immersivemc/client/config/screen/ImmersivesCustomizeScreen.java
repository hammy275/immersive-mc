package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ImmersiveMCConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Optional;

public class ImmersivesCustomizeScreen extends Screen {

    protected final Screen lastScreen;
    protected OptionsList list;

    protected static int BUTTON_WIDTH = 256;
    protected static int BUTTON_HEIGHT = 20;


    public ImmersivesCustomizeScreen(Screen lastScreen) {
        super(Component.translatable("screen.immersivemc.immersives_customize.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();

        this.list = new OptionsList(Minecraft.getInstance(), this.width, this.height,
                32, this.height - 32, 24);

        ScreenUtils.addOption("disable_vanilla_guis", ImmersiveMCConfig.disableVanillaGUIs, this.list);
        ScreenUtils.addOption("return_items", ImmersiveMCConfig.returnItems, this.list);
        ScreenUtils.addOption("do_rumble", ImmersiveMCConfig.doRumble, this.list);
        ScreenUtils.addOption("center_brewing", ImmersiveMCConfig.autoCenterBrewing, this.list);
        ScreenUtils.addOption("center_furnace", ImmersiveMCConfig.autoCenterFurnace, this.list);
        ScreenUtils.addOption("right_click_chest", ImmersiveMCConfig.rightClickChest, this.list);
        ScreenUtils.addOption("spin_crafting_output", ImmersiveMCConfig.spinCraftingOutput, this.list);
        ScreenUtils.addOption("pet_any_living", ImmersiveMCConfig.canPetAnyLiving, this.list);
        ScreenUtils.addOption("right_click_in_vr", ImmersiveMCConfig.rightClickInVR, this.list);
        ScreenUtils.addOption("3d_compat", ImmersiveMCConfig.resourcePack3dCompat, this.list);
        ScreenUtils.addOption("crouch_bypass_immersion", ImmersiveMCConfig.crouchBypassImmersion, this.list);

        this.list.addBig(
                new OptionInstance<>(
                        "config.immersivemc.placement_mode",
                        placementMode -> Tooltip.create(
                                Component.translatable("config.immersivemc.placement_mode.desc",
                                        I18n.get("config.immersivemc.placement_mode." + placementMode.ordinal()).toLowerCase())),
                        (component, placementMode) -> Component.translatable("config.immersivemc.placement_mode." + placementMode.ordinal()),
                        new OptionInstance.LazyEnum<>(
                                () -> Arrays.asList(PlacementMode.values()),
                                Optional::of,
                                null

                        ),
                        ActiveConfig.FILE.placementMode,
                        (newMode) -> {
                            ImmersiveMCConfig.itemPlacementMode.set(newMode.ordinal());
                            ActiveConfig.loadActive();
                        }
                )
        );

        this.list.addBig(ScreenUtils.createIntSlider(
                "config.immersivemc.ranged_grab_range",
                (val) -> {
                    if (val == -1) {
                        return Component.translatable("config.immersivemc.use_pick_range");
                    }
                    return Component.literal(I18n.get("config.immersivemc.ranged_grab_range") + ": " + val);
                },
                -1, 12,
                ActiveConfig.FILE.rangedGrabRange, (newVal) -> {
                    ImmersiveMCConfig.rangedGrabRange.set(newVal);
                    ActiveConfig.FILE.loadFromFile();
                }
        ));


        this.addRenderableWidget(this.list);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                        (button) -> this.onClose())
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .pos((this.width - BUTTON_WIDTH) / 2, this.height - 26)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);

        super.render(graphics, mouseX, mouseY, partialTicks);

        graphics.drawCenteredString(this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);

    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(lastScreen);
    }
}
