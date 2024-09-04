package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ClientActiveConfig;
import com.hammy275.immersivemc.common.config.ConfigType;
import dev.architectury.platform.Platform;
import net.minecraft.client.CycleOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ProgressOption;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

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

    public static CycleOption<Boolean> createOption(String keyName, Function<ActiveConfig, Boolean> valueGetter,
                                                    BiConsumer<ActiveConfig, Boolean> valueSetter) {
        return CycleOption.createOnOff("config." + ImmersiveMC.MOD_ID + "." + keyName,
                new TranslatableComponent("config." + ImmersiveMC.MOD_ID + "." + keyName + ".desc"),
                (unused) -> valueGetter.apply(ConfigScreen.getAdjustingConfig()),
                (unused, unused2, newVal) -> {
                    valueSetter.accept(ConfigScreen.getAdjustingConfig(), newVal);
                }
        );
    }

    public static CycleOption<Boolean> createOption(String keyName, @Nullable Component tooltip,
                                                       Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return CycleOption.createOnOff(
                keyName,
                tooltip == null ? TextComponent.EMPTY : tooltip,
                (unused) -> getter.get(),
                (unused, unused2, newVal) -> setter.accept(newVal)
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
        return createButton(x, y, width, height, translationString, null, clickHandler);
    }

    public static Button createButton(int x, int y, int width, int height, String translationString, String tooltipTranslationString, Consumer<Button> clickHandler) {
        if (tooltipTranslationString == null) {
            return new Button(x, y, width, height, new TranslatableComponent(translationString), clickHandler::accept);
        } else {
            return new Button(x, y, width, height, new TranslatableComponent(translationString), clickHandler::accept,
                    (button, stack, i, j) -> Minecraft.getInstance().screen.renderTooltip(stack, new TranslatableComponent(tooltipTranslationString), i, j));
        }


    }

    public static void addOptionIfModLoaded(String modId, String keyName, Function<ActiveConfig, Boolean> valueGetter,
                                            BiConsumer<ActiveConfig, Boolean> valueSetter, OptionsList list) {
        if (Platform.isModLoaded(modId)) {
            addOption(keyName, valueGetter, valueSetter, list);
        }
    }

    public static void addOption(String keyName, Function<ActiveConfig, Boolean> valueGetter,
                                 BiConsumer<ActiveConfig, Boolean> valueSetter, OptionsList list) {
        list.addBig(createOption(keyName, valueGetter, valueSetter));
    }

    public static void addOptionIfClient(String keyName, Function<ClientActiveConfig, Boolean> valueGetter,
                                 BiConsumer<ClientActiveConfig, Boolean> valueSetter, OptionsList list) {
        if (ConfigScreen.getAdjustingConfigType() == ConfigType.CLIENT) {
            list.addBig(createOption(keyName, ac -> valueGetter.apply((ClientActiveConfig) ac),
                    (ac, newVal) -> valueSetter.accept((ClientActiveConfig) ac, newVal)));
        }
    }

    public static boolean mouseInBox(int mouseX, int mouseY, int leftX, int bottomY, int rightX, int topY) {
        return mouseX >= leftX && mouseX <= rightX && mouseY >= bottomY && mouseY <= topY;
    }
}
