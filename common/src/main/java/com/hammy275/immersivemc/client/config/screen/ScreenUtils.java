package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ClientActiveConfig;
import com.hammy275.immersivemc.common.config.ConfigType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ScreenUtils {

    public static OptionInstance<Integer> createIntSlider(String titleTranslationKey,
                                                          Function<Integer, Component> displayTextCreator,
                                                          int minValue, int maxValue, Supplier<Integer> valueGetter,
                                                          Consumer<Integer> valueSetter) {
        return new OptionInstance<>(titleTranslationKey, OptionInstance.noTooltip(),
                (component, integer) -> displayTextCreator.apply(integer),
                new OptionInstance.IntRange(minValue, maxValue),
                valueGetter.get(), valueSetter);
    }

    public static OptionInstance<Boolean> createOption(String keyName, Function<ActiveConfig, Boolean> valueGetter,
                                                       BiConsumer<ActiveConfig, Boolean> valueSetter) {
        return OptionInstance.createBoolean(
                "config." + ImmersiveMC.MOD_ID + "." + keyName,
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + ImmersiveMC.MOD_ID + "." + keyName + ".desc")),
                valueGetter.apply(ConfigScreen.getAdjustingConfig()),
                (newVal) -> valueSetter.accept(ConfigScreen.getAdjustingConfig(), newVal)
        );
    }

    public static OptionInstance<Boolean> createOption(String keyName, @Nullable Component tooltip,
                                                       Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return OptionInstance.createBoolean(
                keyName,
                tooltip == null ? (obj) -> null : OptionInstance.cachedConstantTooltip(tooltip),
                getter.get(),
                setter
        );
    }

    public static <E extends Enum<E>> OptionInstance<E> createEnumOption(Class<E> enumClass, String titleTranslationKey,
                                                                         Function<E, Component> displayTextCreator,
                                                                         Function<E, Component> tooltipTextCreator,
                                                                         Supplier<E> valueGetter,
                                                                         BiConsumer<Integer, E> valueSetter) {
        return new OptionInstance<E>(
                titleTranslationKey,
                (value) -> Tooltip.create(tooltipTextCreator.apply(value)),
                (component, value) -> displayTextCreator.apply(value),
                new OptionInstance.LazyEnum<>(
                        () -> Arrays.asList(enumClass.getEnumConstants()),
                        Optional::of,
                        null),
                valueGetter.get(),
                (newValue) -> valueSetter.accept(newValue.ordinal(), newValue)
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
        return Button.builder(Component.translatable(translationString), clickHandler::accept)
                .size(width, height)
                .pos(x, y)
                .tooltip(tooltipTranslationString == null ? null : Tooltip.create(Component.translatable(tooltipTranslationString)))
                .build();
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
