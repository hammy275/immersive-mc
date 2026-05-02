package com.hammy275.immersivemc.common.vr.dev.impl;

import org.vivecraft.api.data.FBTMode;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;

public record DevVRPose(DevVRBodyPartyData head, DevVRBodyPartyData c0, DevVRBodyPartyData c1) implements VRPose {

    @Override
    public VRBodyPartData getBodyPartData(VRBodyPart vrBodyPart) {
        return switch (vrBodyPart) {
            case HEAD -> head;
            case MAIN_HAND -> c0;
            case OFF_HAND -> c1;
            default -> null;
        };
    }

    @Override
    public boolean isSeated() {
        return false;
    }

    @Override
    public boolean isLeftHanded() {
        return false;
    }

    @Override
    public FBTMode getFBTMode() {
        return FBTMode.ARMS_ONLY;
    }
}
