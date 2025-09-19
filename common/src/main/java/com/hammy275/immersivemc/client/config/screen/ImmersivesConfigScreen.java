package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.common.compat.apotheosis.Apoth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ImmersivesConfigScreen extends OptionsSubScreen {

    protected final ScreenType type;


    protected static int BUTTON_WIDTH = 128;
    protected static int BUTTON_HEIGHT = 20;

    public ImmersivesConfigScreen(Screen screen, ScreenType type) {
        super(screen, Minecraft.getInstance().options, Component.translatable("screen.immersivemc.immersives_config.title"));
        this.type = type;
    }

    @Override
    protected void addOptions() {
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
            if (Platform.isModLoaded("apotheosis") && Apoth.apothImpl.enchantModuleEnabled()) {
                options.add(ScreenUtils.createOption("enchanting_table_apoth",
                        config -> config.useApotheosisEnchantmentTableImmersive,
                        (config, newVal) -> config.useApotheosisEnchantmentTableImmersive = newVal));
                options.add(ScreenUtils.createOption("apoth_salvaging_table",
                        config -> config.useApotheosisSalvagingTableImmersive,
                        (config, newVal) -> config.useApotheosisSalvagingTableImmersive = newVal));
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, Component.translatable("screen.immersivemc.immersives_config.subtitle"),
                this.width / 2, this.font.lineHeight + 13, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        ConfigScreen.writeAdjustingConfig();
        super.onClose();
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
