package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ImmersivesConfigScreen extends Screen {

    protected final ScreenType type;

    protected final Screen lastScreen;
    protected OptionsList list;


    protected static int BUTTON_WIDTH = 128;
    protected static int BUTTON_HEIGHT = 20;

    public ImmersivesConfigScreen(Screen screen, ScreenType type) {
        super(Component.translatable("screen.immersivemc.immersives_config.title"));
        this.lastScreen = screen;
        this.type = type;
    }

    @Override
    protected void init() {
        super.init();

        this.list = new OptionsList(Minecraft.getInstance(), this.width, this.height,
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
        List<OptionInstance<Boolean>> options = new ArrayList<>();
        if (this.type.isNonVR()) {
            if (Platform.isModLoaded("tconstruct")) {
                options.add(ScreenUtils.createOption("tinkers_construct_crafting_station",
                        config -> config.useTinkersConstructCraftingStationImmersive,
                        (config, newVal) -> config.useTinkersConstructCraftingStationImmersive = newVal));
            }
            if (Platform.isModLoaded("ironfurnaces")) {
                options.add(ScreenUtils.createOption("iron_furnaces_furnace",
                        config -> config.useIronFurnacesFurnaceImmersive,
                        (config, newVal) -> config.useIronFurnacesFurnaceImmersive = newVal));
            }
        }

        if (this.type.isVR()) {
            options.add(ScreenUtils.createOption("animals", config -> config.useFeedingAnimalsImmersive, (config, newVal) -> config.useFeedingAnimalsImmersive = newVal));
            options.add(ScreenUtils.createOption("armor",config -> config.useArmorImmersive, (config, newVal) -> config.useArmorImmersive = newVal));
            options.add(ScreenUtils.createOption("backpack_button", config -> config.useBagImmersive, (config, newVal) -> config.useBagImmersive = newVal));
            options.add(ScreenUtils.createOption("bottle_bucket", config -> config.useBucketAndBottleImmersive, (config, newVal) -> config.useBucketAndBottleImmersive = newVal));
            options.add(ScreenUtils.createOption("button", config -> config.useButtonImmersive, (config, newVal) -> config.useButtonImmersive = newVal));
            options.add(ScreenUtils.createOption("campfire", config -> config.useCampfireImmersive, (config, newVal) -> config.useCampfireImmersive = newVal));
            options.add(ScreenUtils.createOption("cauldron", config -> config.useCauldronImmersive, (config, newVal) -> config.useCauldronImmersive = newVal));
            options.add(ScreenUtils.createOption("door", config -> config.useDoorImmersive, (config, newVal) -> config.useDoorImmersive = newVal));
            options.add(ScreenUtils.createOption("pet", config -> config.allowPetting, (config, newVal) -> config.allowPetting = newVal));
            options.add(ScreenUtils.createOption("ranged_grab", config -> config.useRangedGrabImmersive, (config, newVal) -> config.useRangedGrabImmersive = newVal));
            options.add(ScreenUtils.createOption("shield", config -> config.useShieldImmersive, (config, newVal) -> config.useShieldImmersive = newVal));
            options.add(ScreenUtils.createOption("throw", config -> config.useThrowingImmersive, (config, newVal) -> config.useThrowingImmersive = newVal));
            options.add(ScreenUtils.createOption("written_book", config -> config.useWrittenBookImmersive, (config, newVal) -> config.useWrittenBookImmersive = newVal));
        }

        Immersives.IMMERSIVES.stream()
                .filter((immersive) -> (this.type.isVR() || (this.type.isNonVR() && !immersive.isVROnly())))
                .map(Immersive::configScreenInfo)
                .filter(Objects::nonNull)
                .map((configInfo) -> ScreenUtils.createOption(configInfo.getOptionTranslation(), configInfo.getOptionTooltip(),
                        configInfo::isEnabled, configInfo::setEnabled))
                .forEach(options::add);

        options.stream()
                .sorted(Comparator.comparing(OptionInstance::toString))
                .forEach((option) -> this.list.addBig(option));

    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);

        super.render(stack, mouseX, mouseY, partialTicks);

        drawCenteredString(stack, this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);
        drawCenteredString(stack, this.font, Component.translatable("screen.immersivemc.immersives_config.subtitle"),
                this.width / 2, 8 + this.font.lineHeight, 0xFFFFFF);

        if (this.list != null) {  // Could be null if we're waiting on init(), I believe
            List<FormattedCharSequence> list = OptionsSubScreen.tooltipAt(this.list, mouseX, mouseY);
            if (!list.isEmpty()) {
                this.renderTooltip(stack, list, mouseX, mouseY);
            }
        }
    }

    @Override
    public void onClose() {
        ConfigScreen.writeAdjustingConfig();
        Minecraft.getInstance().setScreen(lastScreen);
    }

    public enum ScreenType {
        VR, // Goes unused, but I may use it at some point
        NONVR,
        BOTH;

        public boolean isVR() {
            return this == VR || this == BOTH;
        }

        public boolean isNonVR() {
            return this == NONVR || this == BOTH;
        }
    }
}
