package com.hammy275.immersivemc.common.compat.frycs_parry;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.common.compat.CompatData;
import com.hammy275.immersivemc.common.compat.util.CompatUtils;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FrycsParryImpl implements FrycsParry {

    public static final CompatData compatData = new CompatData("Fryc's Parry",
            (config, newVal) -> useCompat = false);
    private static boolean useCompat = Platform.isModLoaded("frycparry");

    private static final Class<?> parryHelperClass = CompatUtils.getClazz("net.fryc.frycparry.util.ParryHelper");
    private static final Method isItemParryEnabled = CompatUtils.getMethod(parryHelperClass, "isItemParryEnabled", ItemStack.class);

    public boolean isShield(ItemStack stack) {
        if (!useCompat) {
            return false;
        }
        try {
            return (boolean) isItemParryEnabled.invoke(null, stack);
        } catch (IllegalAccessException | InvocationTargetException ignored) {}
        return false;
    }
}
