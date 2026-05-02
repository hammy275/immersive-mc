package com.hammy275.immersivemc.common.vr.dev.impl;

import com.mojang.math.Quaternion;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.data.VRBodyPartData;

public record DevVRBodyPartyData(Vec3 pos, Vec3 rot) implements VRBodyPartData {
    
    @Override
    public Vec3 getPos() {
        return pos;
    }

    @Override
    public Vec3 getDir() {
        return rot;
    }

    @Override
    public double getPitch() {
        return Math.asin(rot.y / rot.length());
    }

    @Override
    public double getYaw() {
        return Math.atan2(-rot.x, rot.z);
    }

    @Override
    public double getRoll() {
        return 0;
    }

    @Override
    public Quaternion getRotation() {
        return null;
    }
}
