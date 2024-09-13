package com.hammy275.immersivemc;

import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.common.ImmersiveMCRegistrationEvent;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.model.BackpackBundleModel;
import com.hammy275.immersivemc.client.model.BackpackCraftingModel;
import com.hammy275.immersivemc.client.model.BackpackLowDetailModel;
import com.hammy275.immersivemc.client.model.BackpackModel;
import com.hammy275.immersivemc.client.model.Cube1x1;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class ImmersiveMCClient {

    public static final Consumer<ImmersiveMCRegistrationEvent<Immersive<?, ?>>> immersiveIMCRegistrationHandler = (event) -> event.register(
            Immersives.immersiveAnvil, Immersives.immersiveBarrel, Immersives.immersiveBeacon,
            Immersives.immersiveBrewing, Immersives.immersiveChest,
            Immersives.immersiveCrafting, Immersives.immersiveETable, Immersives.immersiveFurnace,
            Immersives.immersiveHopper, Immersives.immersiveIronFurnacesFurnace, Immersives.immersiveJukebox,
            Immersives.immersiveLectern,
            Immersives.immersiveLever, Immersives.immersiveRepeater, Immersives.immersiveShulker,
            Immersives.immersiveSmithingTable, Immersives.immersiveTinkersConstructCraftingStation,
            Immersives.immersiveTrapdoor
    );

    public static void init() {
        // Map to a very obscure key, so it has no conflicts for VR users
        ImmersiveMC.SUMMON_BACKPACK = new KeyMapping("key." + ImmersiveMC.MOD_ID + ".backpack",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F23, ImmersiveMC.vrKeyCategory);
        ImmersiveMC.OPEN_SETTINGS = new KeyMapping("key." + ImmersiveMC.MOD_ID + ".config",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_COMMA, ImmersiveMC.globalKeyCategory);
        ImmersiveMC.RANGED_GRAB_KEY = new KeyMapping("key." + ImmersiveMC.MOD_ID + ".ranged_grab",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F24, ImmersiveMC.vrKeyCategory);
        PlatformClient.registerKeyMapping(ImmersiveMC.SUMMON_BACKPACK);
        PlatformClient.registerKeyMapping(ImmersiveMC.OPEN_SETTINGS);
        PlatformClient.registerKeyMapping(ImmersiveMC.RANGED_GRAB_KEY);

        PlatformClient.registerEntityModelLayer(BackpackCraftingModel.LAYER_LOCATION, BackpackCraftingModel::createBodyLayer);
        PlatformClient.registerEntityModelLayer(BackpackLowDetailModel.LAYER_LOCATION, BackpackLowDetailModel::createBodyLayer);
        PlatformClient.registerEntityModelLayer(BackpackModel.LAYER_LOCATION, BackpackModel::createBodyLayer);
        PlatformClient.registerEntityModelLayer(BackpackBundleModel.LAYER_LOCATION, BackpackBundleModel::createBodyLayer);
        PlatformClient.registerEntityModelLayer(Cube1x1.LAYER_LOCATION, Cube1x1::createBodyLayer);
    }
}
