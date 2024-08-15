package com.hammy275.immersivemc.common.vr;

import com.hammy275.immersivemc.common.util.PosRot;
import net.blf02.vrapi.api.data.IVRData;

public class VRUtil {

    public static PosRot posRot(IVRData data) {
        return new PosRot(data.position(), data.getLookAngle(), data.getPitch(), data.getYaw(), data.getRoll());
    }
}
