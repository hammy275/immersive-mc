package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfoFactory;
import com.hammy275.immersivemc.client.immersive_item.info.ClientBookData;
import com.hammy275.immersivemc.common.util.PageChangeState;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LecternInfo implements ImmersiveInfo {

    public ClientBookData bookData = new ClientBookData(false);
    public long tickCount = 0;

    public LecternInfo(BlockPos pos) {
        bookData.pos = pos;
    }

    @Override
    public List<? extends HitboxInfo> getAllHitboxes() {
        List<HitboxInfo> hitboxes = new ArrayList<>(Arrays.stream(bookData.pageTurnBoxes)
                .map(obb -> HitboxInfoFactory.instance().interactHitbox(obb))
                .toList());
        if (bookData.pageChangeState == PageChangeState.NONE || bookData.pageChangeState.isAnim) {
            // Set hitbox 2 to null when not doing a page turn to prevent it being intersected before clickInfos
            hitboxes.set(2, null);
        }
        hitboxes.addAll(bookData.clickInfos.stream()
                .map(bookClickInfo -> HitboxInfoFactory.instance().triggerHitbox(bookClickInfo.obb()))
                .toList());
        return hitboxes;
    }

    @Override
    public boolean hasHitboxes() {
        return bookData.pageTurnBoxes[2] != null;
    }

    @Override
    public BlockPos getBlockPosition() {
        return bookData.pos;
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
}
