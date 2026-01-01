package com.hammy275.immersivemc;

import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.common.ImmersiveMCRegistrationEvent;
import com.hammy275.immersivemc.client.compat.ipn.IPN;
import com.hammy275.immersivemc.client.compat.ipn.IPNCompat;
import com.hammy275.immersivemc.client.compat.ipn.IPNCompatImpl;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.interact_module.BagOpenInteractModule;
import com.hammy275.immersivemc.client.model.*;
import com.hammy275.immersivemc.common.compat.util.CompatModule;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.api.client.VRClientAPI;

import java.util.function.Consumer;

public class ImmersiveMCClient {

    public static final Consumer<ImmersiveMCRegistrationEvent<Immersive<?, ?>>> immersiveIMCRegistrationHandler = (event) -> event.register(
            Immersives.immersiveAnvil, Immersives.immersiveBarrel, Immersives.immersiveBeacon,
            Immersives.immersiveBrewing, Immersives.immersiveChest, Immersives.immersiveChiseledBookshelf,
            Immersives.immersiveCrafting, Immersives.immersiveETable, Immersives.immersiveFurnace,
            Immersives.immersiveHopper, Immersives.immersiveIronFurnacesFurnace, Immersives.immersiveJukebox,
            Immersives.immersiveLectern,
            Immersives.immersiveLever, Immersives.immersiveRepeater, Immersives.immersiveShulker,
            Immersives.immersiveSmithingTable, Immersives.immersiveTinkersConstructCraftingStation,
            Immersives.immersiveTrapdoor, Immersives.immersiveApothSalvagingTable,
            Immersives.immersiveDoor, Immersives.immersiveGrindstone, Immersives.immersiveVisualWorkbench
    );

    public static void init() {
        KeyMapping.Category globalKeyCategory = KeyMapping.Category.register(Util.id("global"));
        KeyMapping.Category vrKeyCategory = KeyMapping.Category.register(Util.id("vr"));
        // Map to a very obscure key, so it has no conflicts for VR users
        ImmersiveMC.SUMMON_BACKPACK = new KeyMapping("key." + ImmersiveMC.MOD_ID + ".backpack",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F23, vrKeyCategory);
        ImmersiveMC.OPEN_SETTINGS = new KeyMapping("key." + ImmersiveMC.MOD_ID + ".config",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_COMMA, globalKeyCategory);
        ImmersiveMC.RANGED_GRAB_KEY = new KeyMapping("key." + ImmersiveMC.MOD_ID + ".ranged_grab",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F24, vrKeyCategory);
        PlatformClient.registerKeyMapping(ImmersiveMC.SUMMON_BACKPACK);
        PlatformClient.registerKeyMapping(ImmersiveMC.OPEN_SETTINGS);
        PlatformClient.registerKeyMapping(ImmersiveMC.RANGED_GRAB_KEY);

        PlatformClient.registerEntityModelLayer(BackpackCraftingModel.LAYER_LOCATION, BackpackCraftingModel::createBodyLayer);
        PlatformClient.registerEntityModelLayer(BackpackLowDetailModel.LAYER_LOCATION, BackpackLowDetailModel::createBodyLayer);
        PlatformClient.registerEntityModelLayer(BackpackModel.LAYER_LOCATION, BackpackModel::createBodyLayer);
        PlatformClient.registerEntityModelLayer(BackpackBundleModel.LAYER_LOCATION, BackpackBundleModel::createBodyLayer);
        PlatformClient.registerEntityModelLayer(Cube1x1.LAYER_LOCATION, Cube1x1::createBodyLayer);

        PlatformClient.registerPictureInPictureRenderer(CustomGuiRendererState.class, CustomGuiRenderer::new);

        if (Platform.isModLoaded("inventoryprofilesnext")) {
            IPN.ipnCompat = CompatModule.create(new IPNCompatImpl(), IPNCompat.class, IPN.compatData);
        }

        if (VRVerify.hasAPI) {
            VRClientAPI.instance().addClientRegistrationHandler(event ->
                    event.registerInteractModules(new BagOpenInteractModule()));
        }
    }
}
