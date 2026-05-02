package com.hammy275.immersivemc.common.vr;

import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.vr.dev.impl.DevVRAPI;
import com.hammy275.immersivemc.common.vr.dev.impl.DevVRClientAPI;
import com.hammy275.immersivemc.common.vr.dev.impl.DevVRServerAPI;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.server.VRServerAPI;

public class VR {

    public static VRAPI API = VRAPI.instance();
    public static VRClientAPI ClientAPI = VRClientAPI.instance();
    public static VRServerAPI ServerAPI = VRServerAPI.instance();

    static {
        if (CommonConstants.devFakeVRMode) {
            API = new DevVRAPI();
            ClientAPI = new DevVRClientAPI();
            ServerAPI = new DevVRServerAPI();
        }
    }

}
