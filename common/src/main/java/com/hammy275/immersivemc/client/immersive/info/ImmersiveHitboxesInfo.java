package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

public class ImmersiveHitboxesInfo extends AbstractPlayerAttachmentInfo implements InfoTriggerHitboxes {

    // NOTE: This class should have the regular hitboxes and trigger hitboxes share the same
    // index numbers. Have the actual immersive handle both behaviors.
    public static final int BACKPACK_BACK_INDEX = 0;

    private BoundingBox backpackBackHitbox = null;

    public ImmersiveHitboxesInfo() {
        super(ClientConstants.ticksToRenderHitboxesImmersive);
        this.inputHitboxes = new BoundingBox[0];
    }

    @Override
    public void setInputSlots() {
        // No input boxes, so no setting done here
    }

    @Override
    public BoundingBox getHitbox(int slot) {
        if (slot == BACKPACK_BACK_INDEX) {
            return backpackBackHitbox;
        }
        throw new IllegalArgumentException(String.format("ImmersiveHitboxes: " +
                "%d out of range for %d hitboxes.", slot, getAllHitboxes().length));
    }

    @Override
    public BoundingBox[] getAllHitboxes() {
        return new BoundingBox[]{backpackBackHitbox};
    }

    @Override
    public void setHitbox(int slot, BoundingBox hitbox) {
        if (slot == BACKPACK_BACK_INDEX) {
            this.backpackBackHitbox = hitbox;
        }
    }

    @Override
    public boolean hasHitboxes() {
        return true;
    }

    @Override
    public Vec3 getPosition(int slot) {
        return backpackBackHitbox.asOBB().getCenter();
    }

    @Override
    public Vec3[] getAllPositions() {
        BoundingBox[] hitboxes = getAllHitboxes();
        Vec3[] positions = new Vec3[hitboxes.length];
        for (int i = 0; i < hitboxes.length; i++) {
            positions[i] = hitboxes[i].asOBB().getCenter();
        }
        return positions;
    }

    @Override
    public void setPosition(int slot, Vec3 position) {
        throw new UnsupportedOperationException("Cannot set position for ImmersiveHitbox. Set hitbox instead!");
    }

    @Override
    public boolean hasPositions() {
        return true;
    }

    @Override
    public boolean readyToRender() {
        return true;
    }

    @Override
    public BlockPos getBlockPosition() {
        return Minecraft.getInstance().player.blockPosition();
    }

    @Override
    public BoundingBox getTriggerHitbox(int hitboxNum) {
        return getHitbox(hitboxNum);
    }

    @Override
    public BoundingBox[] getTriggerHitboxes() {
        return getAllHitboxes();
    }

    @Override
    public InteractionHand getVRHand() {
        return InteractionHand.OFF_HAND;
    }
}
