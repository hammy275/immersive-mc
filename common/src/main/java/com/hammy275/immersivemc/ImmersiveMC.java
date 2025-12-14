package com.hammy275.immersivemc;

import com.hammy275.immersivemc.api.common.ImmersiveMCRegistrationEvent;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.petting.PettingHandler;
import com.hammy275.immersivemc.client.subscribe.ClientLogicSubscriber;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.*;
import com.hammy275.immersivemc.common.subscribe.CommonSubscriber;
import com.hammy275.immersivemc.server.ServerSubscriber;
import com.hammy275.immersivemc.server.command.ImmersiveMCCommand;
import com.hammy275.immersivemc.server.immersive.petting.AnythingLivingPettingHandler;
import com.hammy275.immersivemc.server.immersive.petting.VanillaMobsPettingHandler;
import net.minecraft.client.KeyMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class ImmersiveMC {

    public static final String MOD_ID = "immersivemc";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

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
                    ImmersiveHandlers.trapdoorHandler, ImmersiveHandlers.apothSalvagingTableHandler, ImmersiveHandlers.doorHandler,
                    ImmersiveHandlers.grindstoneHandler, ImmersiveHandlers.visualWorkbenchHandler
            );

    public static final Consumer<ImmersiveMCRegistrationEvent<PettingHandler<?>>> pettingIMCRegistrationHandler =
            (event) -> event.register(
                    new VanillaMobsPettingHandler<>(),
                    new AnythingLivingPettingHandler<>() // Should always be last so other ImmersiveMC petting handlers go first
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
        
        // Commands
        Platform.registerCommands(dispatcher -> {
            ImmersiveMCCommand.createCommand(dispatcher);
        });

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
        Network.INSTANCE.register(SetRepeaterPacket.class, SetRepeaterPacket::encode,
                SetRepeaterPacket::decode, SetRepeaterPacket::handle);
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
