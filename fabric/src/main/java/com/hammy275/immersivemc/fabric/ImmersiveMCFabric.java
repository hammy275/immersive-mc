package com.hammy275.immersivemc.fabric;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.network.Network;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class ImmersiveMCFabric implements ModInitializer {

    public static final ResourceLocation S2C = new ResourceLocation(ImmersiveMC.MOD_ID, "s2c");
    public static final ResourceLocation C2S = new ResourceLocation(ImmersiveMC.MOD_ID, "c2s");

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(C2S, (server, player, handler, buf, responseSender) -> {
            buf.retain();
            server.execute(() -> {
                try {
                    Network.INSTANCE.doReceive(player, buf);
                } finally {
                    buf.release();
                }
            });
        });
        if (Platform.isClient()) {
            ClientPlayNetworking.registerGlobalReceiver(S2C, (client, handler, buf, responseSender) -> {
                buf.retain();
                client.execute(() -> {
                    try {
                        Network.INSTANCE.doReceive(null, buf);
                    } finally {
                        buf.release();
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
