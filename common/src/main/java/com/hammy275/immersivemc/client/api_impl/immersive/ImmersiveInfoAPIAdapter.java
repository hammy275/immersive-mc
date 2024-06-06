package com.hammy275.immersivemc.client.api_impl.immersive;

import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.InfoTriggerHitboxes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public final class ImmersiveInfoAPIAdapter<I extends ImmersiveInfo> extends AbstractImmersiveInfo implements InfoTriggerHitboxes {

    public final I apiInfo;

    public ImmersiveInfoAPIAdapter(I apiInfo) {
        super(Integer.MAX_VALUE);
        this.apiInfo = apiInfo;
    }

    @Override
    public void setInputSlots() {
        // Intentional NO-OP
    }

    @Override
    public BoundingBox getHitbox(int slot) {
        return this.getAllHitboxes()[slot];
    }

    @Override
    public BoundingBox[] getAllHitboxes() {
        return apiInfo.getAllHitboxes().stream().filter((hInfo) -> !hInfo.isTriggerHitbox()).map(HitboxInfo::getHitbox).toList().toArray(new BoundingBox[0]);
    }

    public BoundingBox[] getAllHitboxesAllTypes() {
        return apiInfo.getAllHitboxes().stream().map(HitboxInfo::getHitbox).toList().toArray(new BoundingBox[0]);
    }

    @Override
    public void setHitbox(int slot, BoundingBox hitbox) {
        // Intentional no-op. ImmersiveMC never calls this generically, so we don't need to support it.
    }

    @Override
    public boolean hasHitboxes() {
        return apiInfo.hasHitboxes();
    }

    @Override
    public Vec3 getPosition(int slot) {
        return BoundingBox.getCenter(this.getAllHitboxes()[slot]);
    }

    @Override
    public Vec3[] getAllPositions() {
        BoundingBox[] boxes = this.getAllHitboxes();
        Vec3[] positions = new Vec3[boxes.length];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = BoundingBox.getCenter(boxes[i]);
        }
        return positions;
    }

    @Override
    public void setPosition(int slot, Vec3 position) {
        // Intentional NO-OP
    }

    @Override
    public boolean hasPositions() {
        return this.hasHitboxes();
    }

    @Override
    public boolean readyToRender() {
        return this.hasHitboxes();
    }

    @Override
    public BlockPos getBlockPosition() {
        return apiInfo.getBlockPosition();
    }

    @Override
    public BoundingBox getTriggerHitbox(int hitboxNum) {
        return this.getTriggerHitboxes()[hitboxNum];
    }

    @Override
    public BoundingBox[] getTriggerHitboxes() {
        return apiInfo.getAllHitboxes().stream().filter(HitboxInfo::isTriggerHitbox).map(HitboxInfo::getHitbox).toList().toArray(new BoundingBox[0]);
    }

    @Override
    public int getVRControllerNum() {
        return 0;
    }
}
