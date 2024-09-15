package com.hammy275.immersivemc.fabric;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.network.Network;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class ImmersiveMCFabric implements ModInitializer {

    public static final ResourceLocation S2C = ResourceLocation.fromNamespaceAndPath(ImmersiveMC.MOD_ID, "s2c");
    public static final ResourceLocation C2S = ResourceLocation.fromNamespaceAndPath(ImmersiveMC.MOD_ID, "c2s");

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(BufferPacket.ID, BufferPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(BufferPacket.ID, BufferPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BufferPacket.ID, ((payload, context) -> {
            payload.buffer().retain();
            context.server().execute(() -> {
                try {
                    Network.INSTANCE.doReceive(context.player(), payload.buffer());
                } finally {
                    payload.buffer().release();
                }
            });
        }));
        if (Platform.isClient()) {
            ClientPlayNetworking.registerGlobalReceiver(BufferPacket.ID, (payload, context) -> {
                payload.buffer().retain();
                context.client().execute(() -> {
                    try {
                        Network.INSTANCE.doReceive(null, payload.buffer());
                    } finally {
                        payload.buffer().release();
                    }
                });
            });
        }
        ImmersiveMC.init();
        try {
            Class.forName("net.blf02.vrapi.api.IVRAPI");
            VRPlugin.initVR();
        } catch (ClassNotFoundException e) {
            ImmersiveMC.LOGGER.info("Not loading with mc-vr-api; it wasn't found!");
        }
        if (Platform.isModLoaded("lootr")) {
            Lootr.lootrImpl = LootrCompatImpl.makeCompatImpl();
        }
    }
}
