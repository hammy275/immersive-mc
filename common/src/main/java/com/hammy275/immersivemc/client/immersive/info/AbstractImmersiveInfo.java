package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractImmersiveInfo implements ImmersiveInfo {

    public final List<HitboxItemPair> hitboxes = new ArrayList<>();
    protected final BlockPos pos;
    protected final int[] slotsHovered = new int[]{-1, -1};
    protected long ticksExisted = 0;

    public AbstractImmersiveInfo(BlockPos pos) {
        this.pos = pos;
    }

    public void tick() {
        ticksExisted++;
    }

    @Override
    public List<HitboxItemPair> getAllHitboxes() {
        return hitboxes;
    }

    @Override
    public boolean hasHitboxes() {
        return !hitboxes.isEmpty();
    }

    @Override
    public BlockPos getBlockPosition() {
        return pos;
    }

    @Override
    public void setSlotHovered(int hitboxIndex, int handIndex) {
        slotsHovered[handIndex] = hitboxIndex;
    }

    @Override
    public int getSlotHovered(int handIndex) {
        return slotsHovered[handIndex];
    }

    public boolean isSlotHovered(int hitboxIndex) {
        return slotsHovered[0] == hitboxIndex || slotsHovered[1] == hitboxIndex;
    }

    @Override
    public long getTicksExisted() {
        return ticksExisted;
    }
}
