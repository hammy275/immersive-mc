package net.blf02.immersivemc.client.config.screen;

import net.blf02.immersivemc.ImmersiveMC;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.client.settings.BooleanOption;
import net.minecraftforge.common.ForgeConfigSpec;

public class ScreenUtils {
    public static BooleanOption createOption(String keyName, ForgeConfigSpec.BooleanValue configEntry) {
        return new BooleanOption("config." + ImmersiveMC.MOD_ID + "." + keyName,
                (unused) -> configEntry.get(),
            (unused, newVal) -> {
            configEntry.set(newVal);
            configEntry.save();
            });
    }

    public static void addOption(String keyName, ForgeConfigSpec.BooleanValue configEntry, OptionsRowList list) {
        list.addBig(createOption(keyName, configEntry));
    }
}
