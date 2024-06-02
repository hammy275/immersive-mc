package com.hammy275.immersivemc.client.api_impl.immersive;

import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.obb.BoundingBox;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ImmersiveInfoAPIAdapter<I extends ImmersiveInfo> extends AbstractImmersiveInfo {

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
        return apiInfo.getAllHitboxes().get(slot);
    }

    @Override
    public BoundingBox[] getAllHitboxes() {
        return apiInfo.getAllHitboxes().toArray(new BoundingBox[0]);
    }

    @Override
    public void setHitbox(int slot, BoundingBox hitbox) {
        apiInfo.getAllHitboxes().set(slot, hitbox);
    }

    @Override
    public boolean hasHitboxes() {
        return apiInfo.hasHitboxes();
    }

    @Override
    public Vec3 getPosition(int slot) {
        return BoundingBox.getCenter(this.getHitbox(slot));
    }

    @Override
    public Vec3[] getAllPositions() {
        List<BoundingBox> hitboxes = apiInfo.getAllHitboxes();
        Vec3[] positions = new Vec3[hitboxes.size()];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = BoundingBox.getCenter(hitboxes.get(i));
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
}
