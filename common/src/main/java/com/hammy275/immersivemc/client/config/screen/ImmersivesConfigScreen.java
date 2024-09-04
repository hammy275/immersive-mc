package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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

        this.list = new OptionsList(Minecraft.getInstance(),
                this.width, this.height - 64, 32, 24);

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
                        config -> config.useTinkersConstructCraftingStationImmersion,
                        (config, newVal) -> config.useTinkersConstructCraftingStationImmersion = newVal));
            }
            if (Platform.isModLoaded("ironfurnaces")) {
                options.add(ScreenUtils.createOption("iron_furnaces_furnace",
                        config -> config.useIronFurnacesFurnaceImmersion,
                        (config, newVal) -> config.useIronFurnacesFurnaceImmersion = newVal));
            }
        }

        if (this.type.isVR()) {
            options.add(ScreenUtils.createOption("animals", config -> config.canFeedAnimals, (config, newVal) -> config.canFeedAnimals = newVal));
            options.add(ScreenUtils.createOption("armor",config -> config.useArmorImmersion, (config, newVal) -> config.useArmorImmersion = newVal));
            options.add(ScreenUtils.createOption("backpack_button", config -> config.useBackpack, (config, newVal) -> config.useBackpack = newVal));
            options.add(ScreenUtils.createOption("button", config -> config.useButton, (config, newVal) -> config.useButton = newVal));
            options.add(ScreenUtils.createOption("campfire", config -> config.useCampfireImmersion, (config, newVal) -> config.useCampfireImmersion = newVal));
            options.add(ScreenUtils.createOption("cauldron", config -> config.useCauldronImmersion, (config, newVal) -> config.useCauldronImmersion = newVal));
            options.add(ScreenUtils.createOption("door", config -> config.useDoorImmersion, (config, newVal) -> config.useDoorImmersion = newVal));
            options.add(ScreenUtils.createOption("pet", config -> config.canPet, (config, newVal) -> config.canPet = newVal));
            options.add(ScreenUtils.createOption("ranged_grab", config -> config.useRangedGrab, (config, newVal) -> config.useRangedGrab = newVal));
            options.add(ScreenUtils.createOption("shield", config -> config.immersiveShield, (config, newVal) -> config.immersiveShield = newVal));
            options.add(ScreenUtils.createOption("throw", config -> config.useThrowing, (config, newVal) -> config.useThrowing = newVal));
            options.add(ScreenUtils.createOption("written_book", config -> config.useWrittenBookImmersion, (config, newVal) -> config.useWrittenBookImmersion = newVal));
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);

        super.render(graphics, mouseX, mouseY, partialTicks);

        graphics.drawCenteredString(this.font, this.title.getString(),
                this.width / 2, 8, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("screen.immersivemc.immersives_config.subtitle"),
                this.width / 2, 8 + this.font.lineHeight, 0xFFFFFF);
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
