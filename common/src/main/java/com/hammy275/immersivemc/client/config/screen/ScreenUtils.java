package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import dev.architectury.platform.Platform;
import net.minecraft.client.CycleOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ProgressOption;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                    ActiveConfig.FILE.loadFromFile();
                }
        );
    }

    public static <E extends Enum<E>> CycleOption<Integer> createEnumOption(Class<E> enumClass, String titleTranslationKey,
                                                                         Function<E, Component> displayTextCreator,
                                                                         Function<E, Component> tooltipTextCreator,
                                                                         Supplier<E> valueGetter,
                                                                         BiConsumer<Integer, E> valueSetter) {
        return CycleOption.create(
                titleTranslationKey,
                () -> IntStream.rangeClosed(0, enumClass.getEnumConstants().length - 1).boxed().collect(Collectors.toList()),
                (optionIndex) -> displayTextCreator.apply(enumClass.getEnumConstants()[optionIndex]),
                (ignored) -> valueGetter.get().ordinal(),
                (ignored, ignored2, newIndex) -> valueSetter.accept(newIndex, enumClass.getEnumConstants()[newIndex])
        ).setTooltip(
                (minecraft) -> optionIndex -> minecraft.font.split(tooltipTextCreator.apply(enumClass.getEnumConstants()[optionIndex]), 200)
        );
    }

    public static Button createScreenButton(int x, int y, int width, int height, String translationString, Screen screen) {
        return createButton(x, y, width, height, translationString, (button) -> Minecraft.getInstance().setScreen(screen));
    }

    public static Button createDoneButton(int x, int y, int width, int height, Screen currentScreen) {
        return createButton(x, y, width, height, "gui.done", (button) -> currentScreen.onClose());
    }

    public static Button createButton(int x, int y, int width, int height, String translationString, Consumer<Button> clickHandler) {
        return new Button(x, y, width, height, new TranslatableComponent(translationString), clickHandler::accept);
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
