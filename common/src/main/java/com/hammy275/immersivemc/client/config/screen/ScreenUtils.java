package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Consumer;
import java.util.function.Function;

public class ScreenUtils {

    public static OptionInstance<Integer> createIntSlider(String titleTranslationKey,
                                                          Function<Integer, Component> displayTextCreator,
                                                          int minValue, int maxValue, int startingValue,
                                                          Consumer<Integer> valueSetter) {
        return new OptionInstance<>(titleTranslationKey, OptionInstance.noTooltip(),
                (component, integer) -> displayTextCreator.apply(integer),
                new OptionInstance.IntRange(minValue, maxValue),
                startingValue, valueSetter);
    }

    public static OptionInstance<Boolean> createOption(String keyName, ForgeConfigSpec.BooleanValue configEntry) {
        return OptionInstance.createBoolean(
                "config." + ImmersiveMC.MOD_ID + "." + keyName,
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + ImmersiveMC.MOD_ID + "." + keyName + ".desc")),
                configEntry.get(),
                (newVal) -> {
                    configEntry.set(newVal);
                    configEntry.save();
                    ActiveConfig.loadConfigFromFile();
                }
        );
    }

    public static void addOption(String keyName, ForgeConfigSpec.BooleanValue configEntry, OptionsList list) {
        list.addBig(createOption(keyName, configEntry));
    }

    public static boolean mouseInBox(int mouseX, int mouseY, int leftX, int bottomY, int rightX, int topY) {
        return mouseX >= leftX && mouseX <= rightX && mouseY >= bottomY && mouseY <= topY;
    }
}
