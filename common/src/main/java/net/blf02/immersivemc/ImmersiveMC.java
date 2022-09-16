package net.blf02.immersivemc;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.blf02.immersivemc.client.subscribe.ClientLogicSubscriber;
import net.blf02.immersivemc.client.subscribe.ClientRenderSubscriber;
import net.blf02.immersivemc.common.config.ImmersiveMCConfig;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.*;
import net.blf02.immersivemc.server.ServerSubscriber;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImmersiveMC {

    public static final String MOD_ID = "immersivemc";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final String globalKeyCategory = "key.categories." + MOD_ID;
    public static final String vrKeyCategory = "key.categories." + MOD_ID + ".vr";
    public static KeyMapping SUMMON_BACKPACK = null;
    public static KeyMapping OPEN_SETTINGS = null;

    public ImmersiveMC() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            // ClientLogic
            ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(ClientLogicSubscriber::onClientLogin);
            TickEvent.PLAYER_POST.register(ClientLogicSubscriber::onPlayerTick);
            ClientRawInputEvent.MOUSE_CLICKED_PRE.register(ClientLogicSubscriber::onClick);
            PlayerEvent.PLAYER_QUIT.register(ClientLogicSubscriber::onDisconnect);

            // ClientRender
            MinecraftForge.EVENT_BUS.register(new ClientRenderSubscriber());
        }

        // ServerSubscriber
        BlockEvent.BREAK.register(ServerSubscriber::blockBreak);
        TickEvent.SERVER_POST.register(ServerSubscriber::onServerTick);
        TickEvent.PLAYER_POST.register(ServerSubscriber::onPlayerTick);
        PlayerEvent.PLAYER_JOIN.register(ServerSubscriber::onPlayerJoin);

        if (Platform.getEnvironment() == Env.CLIENT) {
            ImmersiveMCClient.init();
        }
        serverSetup();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ImmersiveMCConfig.GENERAL_SPEC,
                "immersive_mc.toml");
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        if (FMLLoader.getDist() == Dist.CLIENT) {
            ClientInit.init(); // Load in a separate function so the server-side doesn't yell at us
        }

    }

    protected void serverSetup() {
        Network.INSTANCE.register(SwapPacket.class, SwapPacket::encode,
                SwapPacket::decode, SwapPacket::handle);
        Network.INSTANCE.register(ImmersiveBreakPacket.class, ImmersiveBreakPacket::encode,
                ImmersiveBreakPacket::decode, ImmersiveBreakPacket::handle);
        Network.INSTANCE.register(FetchInventoryPacket.class, FetchInventoryPacket::encode,
                FetchInventoryPacket::decode, FetchInventoryPacket::handle);
        Network.INSTANCE.register(ChestOpenPacket.class, ChestOpenPacket::encode,
                ChestOpenPacket::decode, ChestOpenPacket::handle);
        Network.INSTANCE.register(GrabItemPacket.class, GrabItemPacket::encode,
                GrabItemPacket::decode, GrabItemPacket::handle);
        Network.INSTANCE.register(ConfigSyncPacket.class, ConfigSyncPacket::encode,
                ConfigSyncPacket::decode, ConfigSyncPacket::handle);
        Network.INSTANCE.register(GetEnchantmentsPacket.class, GetEnchantmentsPacket::encode,
                GetEnchantmentsPacket::decode, GetEnchantmentsPacket::handle);
        Network.INSTANCE.register(InventorySwapPacket.class, InventorySwapPacket::encode,
                InventorySwapPacket::decode, InventorySwapPacket::handle);
        Network.INSTANCE.register(SetRepeaterPacket.class, SetRepeaterPacket::encode,
                SetRepeaterPacket::decode, SetRepeaterPacket::handle);
        Network.INSTANCE.register(InteractPacket.class, InteractPacket::encode,
                InteractPacket::decode, InteractPacket::handle);
        Network.INSTANCE.register(UpdateStoragePacket.class, UpdateStoragePacket::encode,
                UpdateStoragePacket::decode, UpdateStoragePacket::handle);
        Network.INSTANCE.register(FetchPlayerStoragePacket.class, FetchPlayerStoragePacket::encode,
                FetchPlayerStoragePacket::decode, FetchPlayerStoragePacket::handle);
        Network.INSTANCE.register(GetRecipePacket.class, GetRecipePacket::encode,
                GetRecipePacket::decode, GetRecipePacket::handle);

    }
}
