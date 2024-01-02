package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
<<<<<<< HEAD
import net.minecraft.client.CycleOption;
import net.minecraft.client.ProgressOption;
=======
import dev.architectury.platform.Platform;
import net.minecraft.client.OptionInstance;
>>>>>>> da8ec7b (Iron Furnaces Compat Proper Declarative)
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ScreenUtils {

    public static ProgressOption createIntSlider(String titleTranslationKey,
                                                 Function<Integer, Component> displayTextCreator,
                                                 int minValue, int maxValue, Supplier<Integer> valueGetter,
                                                 Consumer<Integer> valueSetter) {
        return new ProgressOption(
                titleTranslationKey, minValue, maxValue, 1,
                (ignored) -> Double.valueOf(valueGetter.get()),
                (ignored, newVal) -> valueSetter.accept((int) Math.floor(newVal)),
                (ignored, ignored2) -> displayTextCreator.apply(valueGetter.get())
        );
    }

    public static CycleOption<Boolean> createOption(String keyName, ForgeConfigSpec.BooleanValue configEntry) {
        return CycleOption.createOnOff("config." + ImmersiveMC.MOD_ID + "." + keyName,
                new TranslatableComponent("config." + ImmersiveMC.MOD_ID + "." + keyName + ".desc"),
                (unused) -> configEntry.get(),
                (unused, unused2, newVal) -> {
                    configEntry.set(newVal);
                    configEntry.save();
                    ActiveConfig.loadConfigFromFile();
                });
    }

    public static void addOptionIfModLoaded(String modId, String keyName, ForgeConfigSpec.BooleanValue configEntry, OptionsList list) {
        if (Platform.isModLoaded(modId)) {
            addOption(keyName, configEntry, list);
        }
    }

    public static void addOption(String keyName, ForgeConfigSpec.BooleanValue configEntry, OptionsList list) {
        list.addBig(createOption(keyName, configEntry));
    }

    public static boolean mouseInBox(int mouseX, int mouseY, int leftX, int bottomY, int rightX, int topY) {
        return mouseX >= leftX && mouseX <= rightX && mouseY >= bottomY && mouseY <= topY;
    }
}
