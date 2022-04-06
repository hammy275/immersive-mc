package net.blf02.immersivemc.client.vr;

import net.blf02.immersivemc.client.subscribe.ClientVRSubscriber;
import net.blf02.vrapi.api.IVRAPI;
import net.blf02.vrapi.api.VRAPIPlugin;
import net.blf02.vrapi.api.VRAPIPluginProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLLoader;

@VRAPIPlugin
public class VRPlugin implements VRAPIPluginProvider {

    public static IVRAPI API;

    @Override
    public void getVRAPI(IVRAPI ivrapi) {
        API = ivrapi;
        VRPluginVerify.hasAPI = true;
        // Register Client VR Subscriber if we're running client side and we have VR
        if (FMLLoader.getDist() == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(new ClientVRSubscriber());
        }
    }
}
