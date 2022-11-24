package com.hammy275.immersivemc.client.config.screen;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

public class ScreenUtils {
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
}
