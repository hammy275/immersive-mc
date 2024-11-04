package com.hammy275.immersivemc.client.immersive_item;

import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.config.screen.ConfigScreen;
import com.hammy275.immersivemc.client.immersive_item.info.ScreenBookInfo;
import com.hammy275.immersivemc.common.vr.VRUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class ScreenBookImmersive extends AbstractItemImmersive<ScreenBookInfo> {

    public static boolean renderingFakeScreen = false;

    @Override
    protected void render(ScreenBookInfo info, PoseStack stack, IVRData hand) {
        renderingFakeScreen = true;
        try {
            info.bookData.render(stack, ClientUtil.maxLight, VRUtil.posRot(hand));
        } finally {
            renderingFakeScreen = false;
        }
    }

    @Override
    protected void tick(ScreenBookInfo info, IVRData hand, IVRData other) {
        info.bookData.tick(VRUtil.posRot(hand), VRUtil.posRot(other));
        if (info.screen == null) {
            info.shouldRemove = true;
        }
    }

    @Override
    public boolean itemMatches(ItemStack item) {
        return true;
    }

    @Override
    protected ScreenBookInfo createInfo(ItemStack item, InteractionHand hand) {
        return new ScreenBookInfo(item, hand, new ConfigScreen(null));
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean onLeftClick(ScreenBookInfo info, IVRData hand, IVRData other) {
        return false;
    }
}
