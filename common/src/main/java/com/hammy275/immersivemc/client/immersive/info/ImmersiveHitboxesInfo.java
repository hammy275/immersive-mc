package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.client.immersive.ImmersiveRenderState;
import com.hammy275.immersivemc.api.client.immersive.PlayerAttachmentImmersiveInfo;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import com.hammy275.immersivemc.common.api_impl.hitbox.HitboxInfoImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ImmersiveHitboxesInfo implements PlayerAttachmentImmersiveInfo {
    public static final int BAG_BACK_INDEX = 0;

    public final List<HitboxInfoImpl> hitboxes = new ArrayList<>(1);
    public boolean canOpen = false;
    public int slotHovered = -1;
    public long tickCount;

    public ImmersiveHitboxesInfo() {
        hitboxes.add(new HitboxInfoImpl(AABB.ofSize(Vec3.ZERO, 0, 0, 0), false));
    }

    @Override
    public AbstractClientPlayer getOwner() {
        return Minecraft.getInstance().player;
    }

    @Override
    public List<? extends HitboxInfo> getAllHitboxes() {
        return hitboxes;
    }

    @Override
    public boolean hasHitboxes() {
        return hitboxes.get(0) != null;
    }

    @Override
    public void setSlotHovered(int hitboxIndex, int handIndex) {
        if (handIndex == InteractionHand.OFF_HAND.ordinal()) {
            slotHovered = hitboxIndex;
        }
    }

    @Override
    public int getSlotHovered(int handIndex) {
        return handIndex == InteractionHand.MAIN_HAND.ordinal() ? -1 : slotHovered;
    }

    @Override
    public long getTicksExisted() {
        return tickCount;
    }

    public static class RenderState implements ImmersiveRenderState {

        public List<BoundingBox> hitboxes;
        public long ticksExisted;
        public int slotHovered;

        public RenderState() { }

        @Override
        public List<BoundingBox> hitboxes() {
            return hitboxes;
        }

        @Override
        public long ticksExisted() {
            return ticksExisted;
        }

        @Override
        public boolean isSlotHovered(int slot) {
            return slot == slotHovered;
        }
    }
}
