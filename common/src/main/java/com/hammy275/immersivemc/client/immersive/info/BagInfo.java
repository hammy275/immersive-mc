package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.client.immersive.ImmersiveRenderState;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BagInfo extends AbstractPlayerAttachmentImmersiveInfo {

    public Vec3 handPos = Vec3.ZERO;
    public Vec3 lookVec = Vec3.ZERO;
    public Vec3 renderPos = Vec3.ZERO;
    public Vec3 centerTopPos = Vec3.ZERO;
    public Vec3 downVec = Vec3.ZERO;
    public float handPitch = 0;
    public float handYaw = 0;
    public float handRoll = 0;
    public Vec3 backVec = Vec3.ZERO;
    public int argb = 0;
    public int topRow = 0;
    public int light = -1;

    public BagInfo(AbstractClientPlayer owner) {
        super(owner);
        for (int i = 0; i < 27 + 4 + 1; i++) {
            hitboxes.add(new HitboxItemPair(null, ItemStack.EMPTY, true));
        }
    }

    public void setHitbox(int index, BoundingBox box) {
        HitboxItemPair pair = hitboxes.get(index);
        if (pair.box != null) {
            pair.lastPos = BoundingBox.getCenter(pair.box);
        }
        pair.box = box;
    }

    public void gotoNextRow() {
        if (++this.topRow > 2) {
            this.topRow = 0;
        }
    }

    public int getMidRow() {
        int midRow = this.topRow + 1;
        if (midRow > 2) {
            return 0;
        }
        return midRow;
    }

    public int getBotRow() {
        int botRow = getMidRow() + 1;
        if (botRow > 2) {
            return 0;
        }
        return botRow;
    }

    public static class RenderState implements ImmersiveRenderState {

        public List<BoundingBox> hitboxes;
        public List<ItemStack> items;
        public int light;
        public Vec3 renderPos;
        public float handPitch;
        public float handYaw;
        public float handRoll;
        public int argb;
        public long tickCount;

        @Override
        public List<BoundingBox> hitboxes() {
            return hitboxes;
        }

        @Override
        public long ticksExisted() {
            return 0;
        }

        @Override
        public boolean isSlotHovered(int slot) {
            return false;
        }
    }
}
