package net.blf02.immersivemc;

import net.blf02.immersivemc.client.subscribe.ClientLogicSubscriber;
import net.blf02.immersivemc.client.subscribe.ClientRenderSubscriber;
import net.blf02.immersivemc.common.config.ImmersiveMCConfig;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.*;
import net.blf02.immersivemc.server.ServerSubscriber;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Mod(ImmersiveMC.MOD_ID)
public class ImmersiveMC {

    public static final String MOD_ID = "immersivemc";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final String vrKeyCategory = "key.categories." + MOD_ID + ".vr";
    public static KeyBinding SUMMON_BACKPACK = null;

    public ImmersiveMC() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ImmersiveMCConfig.GENERAL_SPEC,
                "immersive_mc.toml");
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST,
                () -> new ImmutablePair<>(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));

    }

    protected void clientSetup(FMLClientSetupEvent event) {
        // Map to a very obscure key, so it has no conflicts for VR users
        SUMMON_BACKPACK = new KeyBinding("key." + MOD_ID + ".backpack", KeyConflictContext.IN_GAME,
                InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_F23, vrKeyCategory);
        event.enqueueWork(() -> {
            MinecraftForge.EVENT_BUS.register(new ClientLogicSubscriber());
            MinecraftForge.EVENT_BUS.register(new ClientRenderSubscriber());
            ClientRegistry.registerKeyBinding(SUMMON_BACKPACK);
        });
    }

    protected void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MinecraftForge.EVENT_BUS.register(new ServerSubscriber());

            int index = 1;
            Network.INSTANCE.registerMessage(index++, SwapPacket.class, SwapPacket::encode,
                    SwapPacket::decode, SwapPacket::handle);
            Network.INSTANCE.registerMessage(index++, DoCraftPacket.class, DoCraftPacket::encode,
                    DoCraftPacket::decode, DoCraftPacket::handle);
            Network.INSTANCE.registerMessage(index++, ImmersiveBreakPacket.class, ImmersiveBreakPacket::encode,
                    ImmersiveBreakPacket::decode, ImmersiveBreakPacket::handle);
            Network.INSTANCE.registerMessage(index++, FetchInventoryPacket.class, FetchInventoryPacket::encode,
                    FetchInventoryPacket::decode, FetchInventoryPacket::handle);
            Network.INSTANCE.registerMessage(index++, ChestOpenPacket.class, ChestOpenPacket::encode,
                    ChestOpenPacket::decode, ChestOpenPacket::handle);
            Network.INSTANCE.registerMessage(index++, GrabItemPacket.class, GrabItemPacket::encode,
                    GrabItemPacket::decode, GrabItemPacket::handle);
            Network.INSTANCE.registerMessage(index++, ConfigSyncPacket.class, ConfigSyncPacket::encode,
                    ConfigSyncPacket::decode, ConfigSyncPacket::handle);
            Network.INSTANCE.registerMessage(index++, GetAnvilOutputPacket.class, GetAnvilOutputPacket::encode,
                    GetAnvilOutputPacket::decode, GetAnvilOutputPacket::handle);
            Network.INSTANCE.registerMessage(index++, DoAnvilPacket.class, DoAnvilPacket::encode,
                    DoAnvilPacket::decode, DoAnvilPacket::handle);
            Network.INSTANCE.registerMessage(index++, GetEnchantmentsPacket.class, GetEnchantmentsPacket::encode,
                    GetEnchantmentsPacket::decode, GetEnchantmentsPacket::handle);
            Network.INSTANCE.registerMessage(index++, DoETablePacket.class, DoETablePacket::encode,
                    DoETablePacket::decode, DoETablePacket::handle);
            Network.INSTANCE.registerMessage(index++, InventorySwapPacket.class, InventorySwapPacket::encode,
                    InventorySwapPacket::decode, InventorySwapPacket::handle);
        });

    }
}
