package com.hammy275.immersivemc;

import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.common.ImmersiveMCRegistrationEvent;
import com.hammy275.immersivemc.client.compat.ipn.IPN;
import com.hammy275.immersivemc.client.compat.ipn.IPNCompat;
import com.hammy275.immersivemc.client.compat.ipn.IPNCompatImpl;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.interact_module.BagOpenInteractModule;
import com.hammy275.immersivemc.client.interact_module.ChestLidInteractModule;
import com.hammy275.immersivemc.client.model.*;
import com.hammy275.immersivemc.client.ticker.FishingReelTicker;
import com.hammy275.immersivemc.client.ticker.RangedGrabTickerClient;
import com.hammy275.immersivemc.client.ticker.ThrowTicker;
import com.hammy275.immersivemc.common.compat.util.CompatModule;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.ticker.TickerInit;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VR;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.hammy275.immersivemc.common.vr.dev.DevVRState;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class ImmersiveMCClient {

    public static final Consumer<ImmersiveMCRegistrationEvent<Immersive<?, ?, ?>>> immersiveIMCRegistrationHandler = (event) -> event.register(
            Immersives.immersiveAnvil, Immersives.immersiveBarrel, Immersives.immersiveBeacon,
            Immersives.immersiveBrewing, Immersives.immersiveChest, Immersives.immersiveChiseledBookshelf,
            Immersives.immersiveCrafting, Immersives.immersiveETable, Immersives.immersiveFurnace,
            Immersives.immersiveHopper, Immersives.immersiveIronFurnacesFurnace, Immersives.immersiveJukebox,
            Immersives.immersiveLectern,
            Immersives.immersiveLever, Immersives.immersiveRepeater, Immersives.immersiveShulker,
            Immersives.immersiveSmithingTable, Immersives.immersiveTinkersConstructCraftingStation,
            Immersives.immersiveTrapdoor, Immersives.immersiveApothSalvagingTable,
            Immersives.immersiveDoor, Immersives.immersiveGrindstone, Immersives.immersiveVisualWorkbench,
            Immersives.immersiveHitboxes, Immersives.immersiveBag
    );

    public static KeyMapping.Category globalKeyCategory;
    public static KeyMapping.Category vrKeyCategory;

    public static void init() {
        globalKeyCategory = KeyMapping.Category.register(Util.id("global"));
        vrKeyCategory = KeyMapping.Category.register(Util.id("vr"));
        // Map to a very obscure key, so it has no conflicts for VR users
        ImmersiveMC.SUMMON_BACKPACK = new KeyMapping("key." + ImmersiveMC.MOD_ID + ".backpack",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F23, vrKeyCategory);
        ImmersiveMC.OPEN_SETTINGS = new KeyMapping("key." + ImmersiveMC.MOD_ID + ".config",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_COMMA, globalKeyCategory);
        ImmersiveMC.RANGED_GRAB_KEY = new KeyMapping("key." + ImmersiveMC.MOD_ID + ".ranged_grab",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F24, vrKeyCategory);
        Platform.CLIENT.registerKeyMapping(ImmersiveMC.SUMMON_BACKPACK);
        Platform.CLIENT.registerKeyMapping(ImmersiveMC.OPEN_SETTINGS);
        Platform.CLIENT.registerKeyMapping(ImmersiveMC.RANGED_GRAB_KEY);

        Platform.CLIENT.registerEntityModelLayer(BackpackCraftingModel.LAYER_LOCATION, BackpackCraftingModel::createBodyLayer);
        Platform.CLIENT.registerEntityModelLayer(BackpackLowDetailModel.LAYER_LOCATION, BackpackLowDetailModel::createBodyLayer);
        Platform.CLIENT.registerEntityModelLayer(BackpackModel.LAYER_LOCATION, BackpackModel::createBodyLayer);
        Platform.CLIENT.registerEntityModelLayer(BackpackBundleModel.LAYER_LOCATION, BackpackBundleModel::createBodyLayer);
        Platform.CLIENT.registerEntityModelLayer(Cube1x1.LAYER_LOCATION, Cube1x1::createBodyLayer);

        Platform.CLIENT.registerPictureInPictureRenderer(CustomGuiRendererState.class, CustomGuiRenderer::new);

        if (Platform.COMMON.isModLoaded("inventoryprofilesnext")) {
            IPN.ipnCompat = CompatModule.create(new IPNCompatImpl(), IPNCompat.class, IPN.compatData);
        }

        if (CommonConstants.devFakeVRMode) {
            DevVRState.clientInit();
        }

        if (VRVerify.hasAPI) {
            VR.ClientAPI.addClientRegistrationHandler(event ->
                    event.registerInteractModules(new BagOpenInteractModule(), ChestLidInteractModule.INSTANCE));
        }

        // Add client tickers
        TickerInit.addClientTicker(new FishingReelTicker());
        TickerInit.addClientTicker(new RangedGrabTickerClient());
        TickerInit.addClientTicker(ThrowTicker.INSTANCE);
    }
}
