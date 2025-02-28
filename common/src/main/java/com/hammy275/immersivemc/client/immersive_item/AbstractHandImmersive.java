package com.hammy275.immersivemc.client.immersive_item;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.client.immersive_item.info.AbstractHandImmersiveInfo;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.mojang.blaze3d.vertex.PoseStack;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHandImmersive<I extends AbstractHandImmersiveInfo> {

    protected List<I> infos = new ArrayList<>();

    protected abstract void render(I info, PoseStack stack, IVRData hand);

    protected abstract void tick(I info, IVRData hand, IVRData other);

    public abstract boolean activeForHand(InteractionHand hand);

    protected abstract I createInfo(InteractionHand hand);

    public abstract boolean isEnabled();

    public abstract boolean onLeftClick(I info, IVRData hand, IVRData other);

    protected boolean handSwapCandidate(InteractionHand newHand) {
        return false;
    }

    public boolean attemptLeftClickAll() {
        IVRPlayer player = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player);
        for (I info : this.infos) {

            boolean handledClick = onLeftClick(info, player.getController(info.hand.ordinal()),
                    player.getController(info.hand == InteractionHand.MAIN_HAND ? 1 : 0));
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

        IVRPlayer player = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player);

        for (I info : infos) {
            this.tick(info, player.getController(info.hand.ordinal()),
                    player.getController(info.hand == InteractionHand.MAIN_HAND ? 1 : 0));
        }
    }

    public void renderAll(PoseStack poseStack) {
        IVRPlayer player = Platform.isDevelopmentEnvironment() ?
                VRPlugin.API.getVRPlayer(Minecraft.getInstance().player) :
                VRPlugin.API.getRenderVRPlayer();
        for (I info : infos) {
            if (!info.shouldRemove && this.isEnabled()) {
                this.render(info, poseStack, player.getController(info.hand.ordinal()));
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
