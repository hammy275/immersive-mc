package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfoFactory;
import com.hammy275.immersivemc.client.immersive_item.info.ClientBookData;
import net.minecraft.core.BlockPos;

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
        return Arrays.stream(bookData.pageTurnBoxes)
                .map(obb -> HitboxInfoFactory.instance().interactHitbox(obb))
                .toList();
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
