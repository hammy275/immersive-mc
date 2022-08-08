package net.blf02.immersivemc.client.config.screen;

import net.blf02.immersivemc.ImmersiveMC;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.ForgeConfigSpec;

public class ScreenUtils {
    public static BooleanOption createOption(String keyName, ForgeConfigSpec.BooleanValue configEntry) {
        return new BooleanOption("config." + ImmersiveMC.MOD_ID + "." + keyName,
                new TranslatableComponent("config." + ImmersiveMC.MOD_ID + "." + keyName + ".desc"),
                (unused) -> configEntry.get(),
            (unused, newVal) -> {
            configEntry.set(newVal);
            configEntry.save();
                ActiveConfig.loadConfigFromFile();
            });
    }

    public static void addOption(String keyName, ForgeConfigSpec.BooleanValue configEntry, OptionsList list) {
        list.addBig(createOption(keyName, configEntry));
    }
}
