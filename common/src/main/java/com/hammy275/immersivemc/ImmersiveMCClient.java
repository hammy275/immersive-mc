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
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class ImmersiveMCClient {

    public static final Consumer<ImmersiveMCRegistrationEvent<Immersive<?, ?>>> immersiveIMCRegistrationHandler = (event) -> event.register(
            Immersives.immersiveAnvil, Immersives.immersiveBarrel, Immersives.immersiveBeacon,
            Immersives.immersiveBrewing, Immersives.immersiveChest,
            Immersives.immersiveCrafting, Immersives.immersiveETable, Immersives.immersiveFurnace,
            Immersives.immersiveHopper, Immersives.immersiveIronFurnacesFurnace, Immersives.immersiveJukebox,
            Immersives.immersiveLever, Immersives.immersiveRepeater, Immersives.immersiveShulker,
            Immersives.immersiveSmithingTable, Immersives.immersiveTinkersConstructCraftingStation
    );

    public static void init() {
        // Map to a very obscure key, so it has no conflicts for VR users
        ImmersiveMC.SUMMON_BACKPACK = new KeyMapping("key." + ImmersiveMC.MOD_ID + ".backpack",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F23, ImmersiveMC.vrKeyCategory);
        ImmersiveMC.OPEN_SETTINGS = new KeyMapping("key." + ImmersiveMC.MOD_ID + ".config",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_COMMA, ImmersiveMC.globalKeyCategory);
        ImmersiveMC.RANGED_GRAB_KEY = new KeyMapping("key." + ImmersiveMC.MOD_ID + ".ranged_grab",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F24, ImmersiveMC.vrKeyCategory);
        KeyMappingRegistry.register(ImmersiveMC.SUMMON_BACKPACK);
        KeyMappingRegistry.register(ImmersiveMC.OPEN_SETTINGS);
        KeyMappingRegistry.register(ImmersiveMC.RANGED_GRAB_KEY);

        EntityModelLayerRegistry.register(BackpackCraftingModel.LAYER_LOCATION, BackpackCraftingModel::createBodyLayer);
        EntityModelLayerRegistry.register(BackpackLowDetailModel.LAYER_LOCATION, BackpackLowDetailModel::createBodyLayer);
        EntityModelLayerRegistry.register(BackpackModel.LAYER_LOCATION, BackpackModel::createBodyLayer);
        EntityModelLayerRegistry.register(BackpackBundleModel.LAYER_LOCATION, BackpackBundleModel::createBodyLayer);
        EntityModelLayerRegistry.register(Cube1x1.LAYER_LOCATION, Cube1x1::createBodyLayer);
    }
}
