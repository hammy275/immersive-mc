package com.hammy275.immersivemc.client.immersive_item;

import com.hammy275.immersivemc.api.client.ImmersiveClientLogicHelpers;
import com.hammy275.immersivemc.client.immersive.book.WrittenBookHelpers;
import com.hammy275.immersivemc.client.immersive_item.info.WrittenBookInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.vr.VRUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.vivecraft.api.data.VRBodyPartData;

public class WrittenBookImmersive extends AbstractItemImmersive<WrittenBookInfo> {

    @Override
    protected void render(WrittenBookInfo info, PoseStack stack, VRBodyPartData hand) {
        if (info.light > -1) {
            info.bookData.render(stack, info.light, VRUtil.posRot(hand));
        }
    }

    @Override
    protected void tick(WrittenBookInfo info, VRBodyPartData hand, VRBodyPartData other) {
        info.didClick = false;
        info.light = ImmersiveClientLogicHelpers.instance().getLight(BlockPos.containing(hand.getPos()));
        info.bookData.interactables.clear();
        WrittenBookHelpers.addInteractablesForThisTick(info, VRUtil.posRot(hand), true);
        WrittenBookHelpers.addInteractablesForThisTick(info, VRUtil.posRot(hand), false);
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
    public boolean onLeftClick(WrittenBookInfo info, VRBodyPartData hand, VRBodyPartData other) {
        return info.didClick;
    }
}
