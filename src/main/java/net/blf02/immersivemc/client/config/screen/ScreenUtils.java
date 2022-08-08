package net.blf02.immersivemc.client.config.screen;

import net.blf02.immersivemc.ImmersiveMC;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.minecraft.client.CycleOption;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.ForgeConfigSpec;

public class ScreenUtils {
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

    public static void addOption(String keyName, ForgeConfigSpec.BooleanValue configEntry, OptionsList list) {
        list.addBig(createOption(keyName, configEntry));
    }
}
