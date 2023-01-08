package com.hammy275.immersivemc.server.data;

import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.world.phys.Vec3;

public class LastTickData {

    public final IVRPlayer lastPlayer;
    public final Vec3 lastPlayerPos;
    public final Vec3 doubleLastPlayerPos;

    public LastTickData(IVRPlayer lastPlayer, Vec3 lastPlayerPos, Vec3 doubleLastPlayerPos) {
        this.lastPlayer = lastPlayer;
        this.lastPlayerPos = lastPlayerPos;
        this.doubleLastPlayerPos = doubleLastPlayerPos;
    }
}
