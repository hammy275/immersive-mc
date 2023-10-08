package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.client.immersive.HitboxInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for infos that Immersives constructed with the builder can use.
 */
public class BuiltImmersiveInfo extends AbstractImmersiveInfo {

    public HitboxInfo[] hitboxes;
    // Key is input slot number, value is a HitboxInfo
    public Map<Integer, HitboxInfo> inputsToHitboxes = new HashMap<>();
    // Holds all hitboxes that map to items. Subset of this.hitboxes.
    public List<HitboxInfo> itemHitboxes = new ArrayList<>();
    private BlockPos pos;

    public BuiltImmersiveInfo(List<HitboxInfo> hitboxes, BlockPos pos, int ticksToExist) {
        super(ticksToExist);
        this.hitboxes = new HitboxInfo[hitboxes.size()];
        for (int i = 0; i < hitboxes.size(); i++) {
            this.hitboxes[i] = (HitboxInfo) hitboxes.get(i).clone(); // Make clone since it stores items and such with
            if (this.hitboxes[i].holdsItems) {
                itemHitboxes.add(this.hitboxes[i]);
            }
        }
        this.pos = pos;
    }

    @Override
    public void setInputSlots() {
        List<AABB> inputsRaw = new ArrayList<>();
        this.inputsToHitboxes.clear();
        for (int i = 0; i < this.hitboxes.length; i++) {
            HitboxInfo hitboxInfo = this.hitboxes[i];
            if (hitboxInfo.isInput) {
                inputsRaw.add(hitboxInfo.getAABB());
                this.inputsToHitboxes.put(i, hitboxInfo);
            }
        }
        if (this.inputHitboxes == null) {
            this.inputHitboxes = new AABB[inputsRaw.size()];
        }
        for (int i = 0; i < inputsRaw.size(); i++) {
            this.inputHitboxes[i] = inputsRaw.get(i);
        }
    }

    @Override
    public AABB getHitbox(int slot) {
        return this.hitboxes[slot].getAABB();
    }

    @Override
    public AABB[] getAllHitboxes() {
        AABB[] hitboxes = new AABB[this.hitboxes.length];
        for (int i = 0; i < this.hitboxes.length; i++) {
            hitboxes[i] = this.hitboxes[i].getAABB();
        }
        return hitboxes;
    }

    @Override
    public void setHitbox(int slot, AABB hitbox) {
        throw new UnsupportedOperationException("Not supported by immersives made via builders.");
    }

    @Override
    public boolean hasHitboxes() {
        for (HitboxInfo hitbox : this.hitboxes) {
            if (!hitbox.hasAABB()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Vec3 getPosition(int slot) {
        return this.hitboxes[slot].getPos();
    }

    @Override
    public Vec3[] getAllPositions() {
        Vec3[] positions = new Vec3[this.hitboxes.length];
        for (int i = 0; i < this.hitboxes.length; i++) {
            positions[i] = this.hitboxes[i].getPos();
        }
        return positions;
    }

    @Override
    public void setPosition(int slot, Vec3 position) {
        throw new UnsupportedOperationException("Not supported by immersives made via builders.");
    }

    @Override
    public boolean hasPositions() {
        for (HitboxInfo hitbox : this.hitboxes) {
            if (!hitbox.hasPos()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean readyToRender() {
        return hasPositions() && hasHitboxes();
    }

    @Override
    public BlockPos getBlockPosition() {
        return this.pos;
    }
}
