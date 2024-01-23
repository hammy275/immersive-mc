package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ImmersiveMCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
        if (canShowConfigScreen()) {
            initNotInWorld();
        }
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                        (button) -> this.onClose())
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .pos((this.width - BUTTON_WIDTH) / 2, this.height - 26)
                .build());
    }

    protected void initNotInWorld() {
        this.list = new OptionsList(Minecraft.getInstance(),
                this.width, this.height - 64, 32, 24);

        initOptionsList();

        this.addRenderableWidget(this.list);
    }

    protected void initOptionsList() {
        if (this.type.isNonVR()) {
            ScreenUtils.addOption("anvil", ImmersiveMCConfig.useAnvilImmersion, this.list);
            ScreenUtils.addOption("barrel", ImmersiveMCConfig.useBarrelImmersion, this.list);
            ScreenUtils.addOption("beacon", ImmersiveMCConfig.useBeaconImmersion, this.list);
            ScreenUtils.addOption("brewing", ImmersiveMCConfig.useBrewingImmersion, this.list);
            ScreenUtils.addOption("chest", ImmersiveMCConfig.useChestImmersion, this.list);
            ScreenUtils.addOption("crafting", ImmersiveMCConfig.useCraftingImmersion, this.list);
            ScreenUtils.addOptionIfModLoaded("tconstruct", "tinkers_construct_crafting_station", ImmersiveMCConfig.useTinkersConstructCraftingStationImmersion, this.list);
            ScreenUtils.addOption("enchanting_table", ImmersiveMCConfig.useETableImmersion, this.list);
            ScreenUtils.addOption("furnace", ImmersiveMCConfig.useFurnaceImmersion, this.list);
            ScreenUtils.addOption("hopper", ImmersiveMCConfig.useHopperImmersion, this.list);
            ScreenUtils.addOptionIfModLoaded("ironfurnaces", "iron_furnaces_furnace", ImmersiveMCConfig.useIronFurnacesFurnaceImmersion, this.list);
            ScreenUtils.addOption("shulker", ImmersiveMCConfig.useShulkerImmersion, this.list);
            ScreenUtils.addOption("smithing_table", ImmersiveMCConfig.useSmithingTableImmersion, this.list);
        }

        if (this.type.isVR()) {
            ScreenUtils.addOption("animals", ImmersiveMCConfig.canFeedAnimals, this.list);
            ScreenUtils.addOption("armor", ImmersiveMCConfig.useArmorImmersion, this.list);
            ScreenUtils.addOption("backpack_button", ImmersiveMCConfig.useBackpack, this.list);
            ScreenUtils.addOption("button", ImmersiveMCConfig.useButton, this.list);
            ScreenUtils.addOption("campfire", ImmersiveMCConfig.useCampfireImmersion, this.list);
            ScreenUtils.addOption("cauldron", ImmersiveMCConfig.useCauldronImmersion, this.list);
            ScreenUtils.addOption("chiseled_bookshelf", ImmersiveMCConfig.useChiseledBookshelfImmersion, this.list);
            ScreenUtils.addOption("door", ImmersiveMCConfig.useDoorImmersion, this.list);
            ScreenUtils.addOption("jukebox", ImmersiveMCConfig.useJukeboxImmersion, this.list);
            ScreenUtils.addOption("lever", ImmersiveMCConfig.useLever, this.list);
            ScreenUtils.addOption("pet", ImmersiveMCConfig.canPet, this.list);
            ScreenUtils.addOption("ranged_grab", ImmersiveMCConfig.useRangedGrab, this.list);
            ScreenUtils.addOption("repeater", ImmersiveMCConfig.useRepeaterImmersion, this.list);
            ScreenUtils.addOption("shield", ImmersiveMCConfig.immersiveShield, this.list);
            ScreenUtils.addOption("throw", ImmersiveMCConfig.useThrowing, this.list);
            ScreenUtils.addOption("written_book", ImmersiveMCConfig.useWrittenBookImmersion, this.list);
        }


    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);

        super.render(graphics, mouseX, mouseY, partialTicks);

        if (canShowConfigScreen()) {
            graphics.drawCenteredString(this.font, this.title.getString(),
                    this.width / 2, 8, 0xFFFFFF);
            graphics.drawCenteredString(this.font, Component.translatable("screen.immersivemc.immersives_config.subtitle"),
                    this.width / 2, 8 + this.font.lineHeight, 0xFFFFFF);
        } else {
            // This is pretty rare, only happening when we're in a world but haven't finished the S2C part of ImmersiveMC's handshake
            graphics.drawCenteredString(this.font, Component.translatable("screen.immersivemc.immersives_config.cant_change"),
                    this.width / 2, this.height / 2, 0xFFFFFF);
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(lastScreen);
        ActiveConfig.loadConfigFromFile();
        ActiveConfig.reloadAfterServer();
    }

    public boolean canShowConfigScreen() {
        return Minecraft.getInstance().level == null ||
                ActiveConfig.serverCopy != null;
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
