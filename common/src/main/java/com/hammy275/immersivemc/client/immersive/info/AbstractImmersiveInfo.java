package com.hammy275.immersivemc.client.immersive.info;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractImmersiveInfo {

    public final List<HitboxItemPair> hitboxes = new ArrayList<>();
    protected final int[] slotsHovered = new int[]{-1, -1};
    protected long ticksExisted = 0;

    public AbstractImmersiveInfo() { }

    public AbstractImmersiveInfo(AbstractImmersiveInfo info) {
        this.hitboxes.addAll(info.hitboxes);
        this.slotsHovered[0] = info.slotsHovered[0];
        this.slotsHovered[1] = info.slotsHovered[1];
        this.ticksExisted = info.ticksExisted;
    }

    public void tick() {
        ticksExisted++;
    }

    // Overrides from ImmersiveInfo
    public List<HitboxItemPair> getAllHitboxes() {
        return hitboxes;
    }

    // Overrides from ImmersiveInfo
    public boolean hasHitboxes() {
        return !hitboxes.isEmpty();
    }

    // Overrides from ImmersiveInfo
    public void setSlotHovered(int hitboxIndex, int handIndex) {
        slotsHovered[handIndex] = hitboxIndex;
    }

    // Overrides from ImmersiveInfo
    public int getSlotHovered(int handIndex) {
        return slotsHovered[handIndex];
    }

    public boolean isSlotHovered(int hitboxIndex) {
        return slotsHovered[0] == hitboxIndex || slotsHovered[1] == hitboxIndex;
    }

    // Overrides from ImmersiveInfo
    public long getTicksExisted() {
        return ticksExisted;
    }
}
