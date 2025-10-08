package com.hammy275.immersivemc.client.immersive_item;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.client.immersive_item.info.AbstractHandImmersiveInfo;
import com.hammy275.immersivemc.common.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHandImmersive<I extends AbstractHandImmersiveInfo> {

    protected List<I> infos = new ArrayList<>();

    protected abstract void render(I info, PoseStack stack, VRBodyPartData hand);

    protected abstract void tick(I info, VRBodyPartData hand, VRBodyPartData other);

    public abstract boolean activeForHand(InteractionHand hand);

    protected abstract I createInfo(InteractionHand hand);

    public abstract boolean isEnabled();

    public abstract boolean onLeftClick(I info, VRBodyPartData hand, VRBodyPartData other);

    protected boolean handSwapCandidate(InteractionHand newHand) {
        return false;
    }

    public boolean attemptLeftClickAll() {
        VRPose pose = VRClientAPI.instance().getPreTickWorldPose();
        for (I info : this.infos) {
            boolean handledClick = onLeftClick(info, pose.getHand(info.hand),
                    pose.getHand(Util.otherHand(info.hand)));
            if (handledClick) {
                return true;
            }
        }
        return false;
    }


    // Only intended to be called externally
    public void registerAndTickAll() {
        if (!this.isEnabled()) {
            infos.clear();
            return;
        }
        List<I> toRemove = new ArrayList<>();
        for (I info : infos) {
            if (!activeForHand(info.hand)) {
                InteractionHand otherHand = Util.otherHand(info.hand);
                if (handSwapCandidate(otherHand)) {
                    info.hand = otherHand;
                } else {
                    info.shouldRemove = true;
                }
            }

            if (info.shouldRemove) {
                toRemove.add(info);
            }
        }

        infos.removeAll(toRemove);

        maybeRegister(InteractionHand.MAIN_HAND);
        maybeRegister(InteractionHand.OFF_HAND);

        VRPose pose = VRClientAPI.instance().getPreTickWorldPose();

        for (I info : infos) {
            this.tick(info, pose.getHand(info.hand),
                    pose.getHand(Util.otherHand(info.hand)));
        }
    }

    public void renderAll(PoseStack poseStack) {
        VRPose pose = VRClientAPI.instance().getWorldRenderPose();
        for (I info : infos) {
            if (!info.shouldRemove && this.isEnabled()) {
                this.render(info, poseStack, pose.getHand(info.hand));
            }
        }
    }

    // Internal helpers

    private void maybeRegister(InteractionHand hand) {
        for (I info : infos) {
            if (info.hand == hand) {
                return;
            }
        }
        if (activeForHand(hand)) {
            this.infos.add(createInfo(hand));
        }
    }
}
