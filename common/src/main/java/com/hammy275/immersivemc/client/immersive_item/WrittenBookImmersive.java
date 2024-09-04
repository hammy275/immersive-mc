package com.hammy275.immersivemc.client.immersive_item;

import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.immersive_item.info.WrittenBookInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.obb.OBBClientUtil;
import com.hammy275.immersivemc.common.vr.VRUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class WrittenBookImmersive extends AbstractItemImmersive<WrittenBookInfo> {

    @Override
    protected void render(WrittenBookInfo info, PoseStack stack, IVRData hand) {
        info.bookData.render(stack, VRUtil.posRot(hand));
    }

    @Override
    protected void tick(WrittenBookInfo info, IVRData hand, IVRData other) {
        info.bookData.tick(VRUtil.posRot(hand), VRUtil.posRot(other));
    }

    @Override
    public boolean itemMatches(ItemStack item) {
        return item.getItem() == Items.WRITTEN_BOOK || item.getItem() == Items.WRITABLE_BOOK;
    }

    @Override
    protected WrittenBookInfo createInfo(ItemStack item, InteractionHand hand) {
        return new WrittenBookInfo(item, hand);
    }

    @Override
    public boolean isEnabled() {
        return ActiveConfig.active().useWrittenBookImmersive;
    }

    @Override
    public boolean onLeftClick(WrittenBookInfo info, IVRData hand, IVRData other) {
        return info.bookData.doPageInteract(0);
    }
}
