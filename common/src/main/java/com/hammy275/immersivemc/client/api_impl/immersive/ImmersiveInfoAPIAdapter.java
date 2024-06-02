package com.hammy275.immersivemc.client.api_impl.immersive;

import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.InfoTriggerHitboxes;
import com.hammy275.immersivemc.common.api_impl.hitbox.HitboxInfoFactoryImpl;
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

    @Override
    public void setHitbox(int slot, BoundingBox hitbox) {
        int runningSlot = 0;
        for (int i = 0; i < apiInfo.getAllHitboxes().size(); i++) {
            HitboxInfo hbox = apiInfo.getAllHitboxes().get(i);
            if (!hbox.isTriggerHitbox()) {
                if (runningSlot == slot) {
                    apiInfo.getAllHitboxes().set(i, HitboxInfoFactoryImpl.INSTANCE.interactHitbox(hitbox));
                } else {
                    runningSlot++;
                }
            }
        }
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
