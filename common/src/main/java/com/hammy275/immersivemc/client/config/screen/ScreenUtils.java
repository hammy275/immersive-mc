package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

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

    public static OptionInstance<Boolean> createOption(String keyName, ForgeConfigSpec.BooleanValue configEntry) {
        return OptionInstance.createBoolean(
                "config." + ImmersiveMC.MOD_ID + "." + keyName,
                OptionInstance.cachedConstantTooltip(Component.translatable("config." + ImmersiveMC.MOD_ID + "." + keyName + ".desc")),
                configEntry.get(),
                (newVal) -> {
                    configEntry.set(newVal);
                    ActiveConfig.FILE.loadFromFile();
                }
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
        return Button.builder(Component.translatable(translationString), clickHandler::accept)
                .size(width, height)
                .pos(x, y)
                .build();
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
