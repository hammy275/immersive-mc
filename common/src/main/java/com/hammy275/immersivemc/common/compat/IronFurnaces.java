package com.hammy275.immersivemc.common.compat;

import com.hammy275.immersivemc.common.compat.util.CompatUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class IronFurnaces {

    public static final Class<?> ironFurnaceTileBase =
            CompatUtils.getClazz("ironfurnaces.tileentity.furnaces.BlockIronFurnaceTileBase");
    public static final CompatData compatData = new CompatData("Iron Furnaces' Furnaces",
            (config, newVal) -> config.useIronFurnacesFurnaceImmersive = newVal);

    private static final Method unlockRecipes = findUnlockRecipesMethod();


    private static Method findUnlockRecipesMethod() {
        Method method = CompatUtils.getMethod(ironFurnaceTileBase, "unlockRecipes", ServerPlayer.class);
        if (method == null) {
            method = CompatUtils.getMethod(ironFurnaceTileBase, "unlockRecipes", Player.class);
        }
        return method;
    }

    public static void doUnlockRecipes(WorldlyContainer furnace, ServerPlayer player) {
        if (ironFurnaceTileBase.isInstance(furnace) && unlockRecipes != null) {
            try {
                unlockRecipes.invoke(furnace, player);
            } catch (IllegalAccessException | InvocationTargetException ignored) {}
        }
    }

}
