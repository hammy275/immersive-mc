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
    public boolean clearLastPos = false;
    public boolean leftHanded = false;
    public boolean otherPlayerSwappedHands = false;
    public BoundingBox dragHitbox;

    public BagInfo(AbstractClientPlayer owner) {
        super(owner);
        for (int i = 0; i < 27 + 4 + 1; i++) {
            hitboxes.add(new HitboxItemPair(null, ItemStack.EMPTY, true));
        }
    }

    public BagInfo(BagInfo info) {
        super(info);
        this.handPos = info.handPos;
        this.lookVec = info.lookVec;
        this.renderPos = info.renderPos;
        this.centerTopPos = info.centerTopPos;
        this.downVec = info.downVec;
        this.handPitch = info.handPitch;
        this.handYaw = info.handYaw;
        this.handRoll = info.handRoll;
        this.backVec = info.backVec;
        this.argb = info.argb;
        this.topRow = info.topRow;
        this.light = info.light;
        this.clearLastPos = info.clearLastPos;
        this.leftHanded = info.leftHanded;
        this.otherPlayerSwappedHands = info.otherPlayerSwappedHands;
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
        clearLastPos = true;
    }

    public int getMidRow() {
        int midRow = this.topRow + 1;
        if (midRow > 2) {
            return 0;
        }
        return midRow;
    }

    public boolean hasSlotHovered() {
        return slotsHovered[0] > -1 || slotsHovered[1] > -1;
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
        public int slotHovered;
        public boolean ownedByLocalPlayer;
        public boolean leftHanded;
        public BoundingBox dragHitbox;

        @Override
        public List<BoundingBox> hitboxes() {
            return hitboxes;
        }

        @Override
        public long ticksExisted() {
            return tickCount;
        }

        @Override
        public boolean isSlotHovered(int slot) {
            return slotHovered == slot;
        }
    }
}
