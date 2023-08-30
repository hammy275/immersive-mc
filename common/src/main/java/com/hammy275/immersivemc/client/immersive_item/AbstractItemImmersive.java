package com.hammy275.immersivemc.client.immersive_item;

import com.hammy275.immersivemc.client.immersive_item.info.AbstractItemInfo;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractItemImmersive<I extends AbstractItemInfo> {

    protected List<I> infos = new ArrayList<>();

    protected abstract void render(I info, PoseStack stack, IVRData hand);

    protected abstract void tick(I info, IVRData hand, IVRData other);

    public abstract boolean itemMatches(ItemStack item);

    protected abstract I createInfo(ItemStack item, InteractionHand hand);

    public abstract boolean isEnabled();

    public abstract boolean onLeftClick(I info, IVRData hand, IVRData other);

    public boolean attemptLeftClickAll() {
        IVRPlayer player = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player);
        for (I info : this.infos) {

            boolean handledClick = onLeftClick(info, player.getController(info.handIn.ordinal()),
                    player.getController(info.handIn == InteractionHand.MAIN_HAND ? 1 : 0));
            if (handledClick) {
                return true;
            }
        }
        return false;
    }


    // Only intended to be called externally
    public void registerAndTickAll(ItemStack mainStack, ItemStack offStack) {
        if (!this.isEnabled()) {
            infos.clear();
            return;
        }
        List<I> toRemove = new ArrayList<>();
        for (I info : infos) {
            ItemStack currentHandItem = info.handIn == InteractionHand.MAIN_HAND ? mainStack : offStack;
            ItemStack currentOtherHandItem = info.handIn == InteractionHand.MAIN_HAND ? offStack : mainStack;
            if (!Util.stacksEqualBesidesCount(info.item, currentHandItem)) {
                if (Util.stacksEqualBesidesCount(info.item, currentOtherHandItem)) {
                    // Swap hands
                    info.handIn = info.handIn == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                } else {
                    // Item left both of our hands, remove it!
                    info.shouldRemove = true;
                }
            }

            if (info.shouldRemove) {
                toRemove.add(info);
            }
        }

        infos.removeAll(toRemove);

        maybeRegister(mainStack, InteractionHand.MAIN_HAND);
        maybeRegister(offStack, InteractionHand.OFF_HAND);

        IVRPlayer player = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player);

        for (I info : infos) {
            this.tick(info, player.getController(info.handIn.ordinal()),
                    player.getController(info.handIn == InteractionHand.MAIN_HAND ? 1 : 0));
        }
    }

    public void renderAll(PoseStack poseStack) {
        IVRPlayer player = Platform.isDevelopmentEnvironment() ?
                VRPlugin.API.getVRPlayer(Minecraft.getInstance().player) :
                VRPlugin.API.getRenderVRPlayer();
        for (I info : infos) {
            if (!info.shouldRemove && this.isEnabled()) {
                this.render(info, poseStack, player.getController(info.handIn.ordinal()));
            }
        }
    }

    // Internal helpers

    private void maybeRegister(ItemStack item, InteractionHand hand) {
        // When this function is called, we've removed all invalid items. So hand matching means items match.
        for (I info : infos) {
            if (info.handIn == hand) {
                return;
            }
        }
        if (itemMatches(item)) {
            this.infos.add(createInfo(item, hand));
        }
    }
}
