package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfoFactory;
import com.hammy275.immersivemc.client.immersive.book.ClientBookData;
import com.hammy275.immersivemc.client.immersive.book.WrittenBookDataHolder;
import com.hammy275.immersivemc.client.immersive.book.WrittenBookHelpers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.PageTurnPacket;
import com.hammy275.immersivemc.common.util.PageChangeState;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LecternInfo implements ImmersiveInfo, WrittenBookDataHolder {

    public ClientLecternData lecternData = new ClientLecternData();
    public long tickCount = 0;
    public int light = -1;
    public boolean didClick = false;

    public LecternInfo(BlockPos pos) {
        lecternData.pos = pos;
        lecternData.bookData.startVRPageTurnCallback = ignored -> Network.INSTANCE.sendToServer(new PageTurnPacket(pos));
        lecternData.level = Minecraft.getInstance().level;
    }

    public void setBook(ItemStack book) {
        if (book.isEmpty()) {
            this.lecternData.bookData.renderables.clear();
            this.lecternData.bookData.interactables.clear();
        } else {
            lecternData.setBook(book, null);
            lecternData.bookData = WrittenBookHelpers.makeClientBookData(this);
        }
    }

    @Override
    public List<? extends HitboxInfo> getAllHitboxes() {
        List<HitboxInfo> hitboxes = new ArrayList<>(lecternData.bookData.getPageTurnHitboxes().stream()
                .map(obb -> HitboxInfoFactory.instance().interactHitbox(obb))
                .toList());
        if (lecternData.bookData.pageChangeState == PageChangeState.NONE || lecternData.bookData.pageChangeState.isAnim) {
            // Set hitbox 2 to null when not doing a page turn to prevent it being intersected before clickInfos
            hitboxes.set(2, null);
        }
        hitboxes.addAll(lecternData.bookData.getInteractableHitboxes().stream()
                .map(obb -> HitboxInfoFactory.instance().triggerHitbox(obb))
                .toList());
        return hitboxes;
    }

    @Override
    public boolean hasHitboxes() {
        return true;
    }

    @Override
    public BlockPos getBlockPosition() {
        return lecternData.pos;
    }

    @Override
    public void setSlotHovered(int hitboxIndex, int handIndex) {
        // Intentional no-op. No slots can be hovered, since we don't hold any items.
    }

    @Override
    public int getSlotHovered(int handIndex) {
        return -1;
    }

    @Override
    public long getTicksExisted() {
        return tickCount;
    }

    @Override
    public ClientBookData getData() {
        return lecternData.bookData;
    }

    @Override
    public ItemStack getBook() {
        return lecternData.book;
    }

    @Override
    public void onPageChangeStyleClick(int newPage) {
        Network.INSTANCE.sendToServer(new PageTurnPacket(getBlockPosition(), newPage));
        didClick = true;
    }
}
