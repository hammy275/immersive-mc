package com.hammy275.immersivemc;

import com.hammy275.immersivemc.api.common.ImmersiveMCRegistrationEvent;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.subscribe.ClientLogicSubscriber;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.BackpackInteractPacket;
import com.hammy275.immersivemc.common.network.packet.BeaconConfirmPacket;
import com.hammy275.immersivemc.common.network.packet.BeaconDataPacket;
import com.hammy275.immersivemc.common.network.packet.ChestShulkerOpenPacket;
import com.hammy275.immersivemc.common.network.packet.ConfigSyncPacket;
import com.hammy275.immersivemc.common.network.packet.DoubleControllerVibrate;
import com.hammy275.immersivemc.common.network.packet.FetchBackpackStoragePacket;
import com.hammy275.immersivemc.common.network.packet.FetchInventoryPacket;
import com.hammy275.immersivemc.common.network.packet.GrabItemPacket;
import com.hammy275.immersivemc.common.network.packet.InventorySwapPacket;
import com.hammy275.immersivemc.common.network.packet.PageTurnPacket;
import com.hammy275.immersivemc.common.network.packet.ReelFishPacket;
import com.hammy275.immersivemc.common.network.packet.SetRepeaterPacket;
import com.hammy275.immersivemc.common.network.packet.SwapPacket;
import com.hammy275.immersivemc.common.network.packet.ThrowPacket;
import com.hammy275.immersivemc.common.network.packet.UsePacket;
import com.hammy275.immersivemc.common.subscribe.CommonSubscriber;
import com.hammy275.immersivemc.server.ServerSubscriber;
import net.minecraft.client.KeyMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class ImmersiveMC {

    public static final String MOD_ID = "immersivemc";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final String globalKeyCategory = "key.categories." + MOD_ID;
    public static final String vrKeyCategory = "key.categories." + MOD_ID + ".vr";
    public static KeyMapping SUMMON_BACKPACK = null;
    public static KeyMapping OPEN_SETTINGS = null;
    public static KeyMapping RANGED_GRAB_KEY = null;

    public static final Consumer<ImmersiveMCRegistrationEvent<ImmersiveHandler<?>>> handlerIMCRegistrationHandler =
            (event) -> event.register(
                    ImmersiveHandlers.anvilHandler, ImmersiveHandlers.barrelHandler, ImmersiveHandlers.beaconHandler,
                    ImmersiveHandlers.brewingStandHandler, ImmersiveHandlers.chestHandler, ImmersiveHandlers.chiseledBookshelfHandler,
                    ImmersiveHandlers.craftingHandler, ImmersiveHandlers.enchantingTableHandler, ImmersiveHandlers.furnaceHandler,
                    ImmersiveHandlers.hopperHandler, ImmersiveHandlers.ironFurnacesFurnaceHandler, ImmersiveHandlers.jukeboxHandler,
                    ImmersiveHandlers.leverHandler, ImmersiveHandlers.repeaterHandler, ImmersiveHandlers.shulkerBoxHandler,
                    ImmersiveHandlers.smithingTableHandler, ImmersiveHandlers.tcCraftingStationHandler, ImmersiveHandlers.lecternHandler,
                    ImmersiveHandlers.trapdoorHandler
            );

    public ImmersiveMC() {
    }

    public static void init() {
        if (Platform.isClient()) {
            // ClientLogic
            PlatformClient.registerOnClientJoinListener(ClientLogicSubscriber::onClientLogin);
            PlatformClient.registerOnClientTickListener(ClientLogicSubscriber::onClientTick);
            PlatformClient.registerOnClientDisconnectListener(ClientLogicSubscriber::onDisconnect);
            PlatformClient.registerOnClientTickListener(CommonSubscriber::globalTick);
        }

        // ServerSubscriber
        Platform.registerServerPostTickListener(ServerSubscriber::onServerTick);
        Platform.registerServerPlayerPostTickListener(ServerSubscriber::onPlayerTick);
        Platform.registerServerPlayerJoinListener(ServerSubscriber::onPlayerJoin);
        Platform.registerServerPlayerLeaveListener(ServerSubscriber::onPlayerLeave);
        Platform.registerServerPlayerPostTickListener(CommonSubscriber::onPlayerTick);
        Platform.registerServerPostTickListener(CommonSubscriber::globalTick);

        if (Platform.isClient()) {
            ImmersiveMCClient.init();
        }
        networkSetup();
        ActiveConfig.createDefaultConfigFilesIfNotFound();
    }

    protected static void networkSetup() {
        Network.INSTANCE.register(SwapPacket.class, SwapPacket::encode,
                SwapPacket::decode, SwapPacket::handle);
        Network.INSTANCE.register(FetchInventoryPacket.class, FetchInventoryPacket::encode,
                FetchInventoryPacket::decode, FetchInventoryPacket::handle);
        Network.INSTANCE.register(ChestShulkerOpenPacket.class, ChestShulkerOpenPacket::encode,
                ChestShulkerOpenPacket::decode, ChestShulkerOpenPacket::handle);
        Network.INSTANCE.register(GrabItemPacket.class, GrabItemPacket::encode,
                GrabItemPacket::decode, GrabItemPacket::handle);
        Network.INSTANCE.register(ConfigSyncPacket.class, ConfigSyncPacket::encode,
                ConfigSyncPacket::decode, ConfigSyncPacket::handle);
        Network.INSTANCE.register(InventorySwapPacket.class, InventorySwapPacket::encode,
                InventorySwapPacket::decode, InventorySwapPacket::handle);
        Network.INSTANCE.register(SetRepeaterPacket.class, SetRepeaterPacket::encode,
                SetRepeaterPacket::decode, SetRepeaterPacket::handle);
        Network.INSTANCE.register(BackpackInteractPacket.class, BackpackInteractPacket::encode,
                BackpackInteractPacket::decode, BackpackInteractPacket::handle);
        Network.INSTANCE.register(FetchBackpackStoragePacket.class, FetchBackpackStoragePacket::encode,
                FetchBackpackStoragePacket::decode, FetchBackpackStoragePacket::handle);
        Network.INSTANCE.register(BeaconConfirmPacket.class, BeaconConfirmPacket::encode,
                BeaconConfirmPacket::decode, BeaconConfirmPacket::handle);
        Network.INSTANCE.register(ThrowPacket.class, ThrowPacket::encode,
                ThrowPacket::decode, ThrowPacket::handle);
        Network.INSTANCE.register(ReelFishPacket.class, ReelFishPacket::encode,
                ReelFishPacket::decode, ReelFishPacket::handle);
        Network.INSTANCE.register(BeaconDataPacket.class, BeaconDataPacket::encode,
                BeaconDataPacket::decode, BeaconDataPacket::handle);
        Network.INSTANCE.register(DoubleControllerVibrate.class, DoubleControllerVibrate::encode,
                DoubleControllerVibrate::decode, DoubleControllerVibrate::handle);
        Network.INSTANCE.register(UsePacket.class, UsePacket::encode,
                UsePacket::decode, UsePacket::handle);
        Network.INSTANCE.register(PageTurnPacket.class, PageTurnPacket::encode,
                PageTurnPacket::decode, PageTurnPacket::handle);

    }
}
