package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.common.config.ImmersiveMCConfig;
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
            options.add(ScreenUtils.createOption("barrel", ImmersiveMCConfig.useBarrelImmersion));
            options.add(ScreenUtils.createOption("beacon", ImmersiveMCConfig.useBeaconImmersion));
            options.add(ScreenUtils.createOption("brewing", ImmersiveMCConfig.useBrewingImmersion));
            options.add(ScreenUtils.createOption("chest", ImmersiveMCConfig.useChestImmersion));
            options.add(ScreenUtils.createOption("crafting", ImmersiveMCConfig.useCraftingImmersion));
            if (Platform.isModLoaded("tconstruct")) {
                options.add(ScreenUtils.createOption("tinkers_construct_crafting_station", ImmersiveMCConfig.useTinkersConstructCraftingStationImmersion));
            }
            options.add(ScreenUtils.createOption("enchanting_table", ImmersiveMCConfig.useETableImmersion));
            options.add(ScreenUtils.createOption("furnace", ImmersiveMCConfig.useFurnaceImmersion));
            options.add(ScreenUtils.createOption("hopper", ImmersiveMCConfig.useHopperImmersion));
            if (Platform.isModLoaded("ironfurnaces")) {
                options.add(ScreenUtils.createOption("iron_furnaces_furnace", ImmersiveMCConfig.useIronFurnacesFurnaceImmersion));
            }
            options.add(ScreenUtils.createOption("shulker", ImmersiveMCConfig.useShulkerImmersion));
            options.add(ScreenUtils.createOption("smithing_table", ImmersiveMCConfig.useSmithingTableImmersion));
        }

        if (this.type.isVR()) {
            options.add(ScreenUtils.createOption("animals", ImmersiveMCConfig.canFeedAnimals));
            options.add(ScreenUtils.createOption("armor", ImmersiveMCConfig.useArmorImmersion));
            options.add(ScreenUtils.createOption("backpack_button", ImmersiveMCConfig.useBackpack));
            options.add(ScreenUtils.createOption("button", ImmersiveMCConfig.useButton));
            options.add(ScreenUtils.createOption("campfire", ImmersiveMCConfig.useCampfireImmersion));
            options.add(ScreenUtils.createOption("cauldron", ImmersiveMCConfig.useCauldronImmersion));
            options.add(ScreenUtils.createOption("chiseled_bookshelf", ImmersiveMCConfig.useChiseledBookshelfImmersion));
            options.add(ScreenUtils.createOption("door", ImmersiveMCConfig.useDoorImmersion));
            options.add(ScreenUtils.createOption("jukebox", ImmersiveMCConfig.useJukeboxImmersion));
            options.add(ScreenUtils.createOption("lever", ImmersiveMCConfig.useLever));
            options.add(ScreenUtils.createOption("pet", ImmersiveMCConfig.canPet));
            options.add(ScreenUtils.createOption("ranged_grab", ImmersiveMCConfig.useRangedGrab));
            options.add(ScreenUtils.createOption("repeater", ImmersiveMCConfig.useRepeaterImmersion));
            options.add(ScreenUtils.createOption("shield", ImmersiveMCConfig.immersiveShield));
            options.add(ScreenUtils.createOption("throw", ImmersiveMCConfig.useThrowing));
            options.add(ScreenUtils.createOption("written_book", ImmersiveMCConfig.useWrittenBookImmersion));
        }

        Immersives.IMMERSIVES.stream()
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
