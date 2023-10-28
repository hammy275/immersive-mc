package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.client.immersive.HitboxInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for infos that Immersives constructed with the builder can use.
 */
public class BuiltImmersiveInfo extends AbstractImmersiveInfo implements InfoTriggerHitboxes {

    public HitboxInfo[] hitboxes;
    // Key is input slot number, value is a HitboxInfo
    public Map<Integer, HitboxInfo> inputsToHitboxes = new HashMap<>();
    // Holds all hitboxes that map to items. Subset of this.hitboxes.
    public List<HitboxInfo> itemHitboxes = new ArrayList<>();
    public List<HitboxInfo> triggerHitboxes = new ArrayList<>();
    // Maps triggerHitbox indices to hitboxes indices
    public Map<Integer, Integer> triggerToRegularHitbox = new HashMap<>();
    private BlockPos pos;
    private int triggerControllerNum;
    public final Object extraData;

    public BuiltImmersiveInfo(List<HitboxInfo> hitboxes, BlockPos pos, int ticksToExist,
                              int triggerControllerNum, Class<?> extraDataClazz) {
        super(ticksToExist);
        this.hitboxes = new HitboxInfo[hitboxes.size()];
        for (int i = 0; i < hitboxes.size(); i++) {
            this.hitboxes[i] = (HitboxInfo) hitboxes.get(i).clone(); // Make clone since it stores items and such with
            if (this.hitboxes[i].holdsItems) {
                itemHitboxes.add(this.hitboxes[i]);
            }
            if (this.hitboxes[i].isTriggerHitbox) {
                triggerHitboxes.add(this.hitboxes[i]);
                triggerToRegularHitbox.put(triggerHitboxes.size() - 1, i);
            }
        }
        this.pos = pos;
        this.triggerControllerNum = triggerControllerNum;
        try {
            this.extraData = extraDataClazz == null ? null : extraDataClazz.getDeclaredConstructor(null).newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
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
        // Only need to have one hitbox to be valid.
        for (HitboxInfo hitbox : this.hitboxes) {
            if (hitbox.hasAABB()) {
                return true;
            }
        }
        return false;
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
        // Only need to have one position to be valid.
        for (HitboxInfo hitbox : this.hitboxes) {
            if (hitbox.hasPos()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean readyToRender() {
        // All hitboxes must be calculated, even if the result is null
        for (HitboxInfo hitbox : this.hitboxes) {
            if (!hitbox.calcDone()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public BlockPos getBlockPosition() {
        return this.pos;
    }

    @Override
    public AABB getTriggerHitbox(int hitboxNum) {
        return this.triggerHitboxes.get(hitboxNum).getAABB();
    }

    @Override
    public AABB[] getTriggerHitboxes() {
        AABB[] triggerHitboxes = new AABB[this.triggerHitboxes.size()];
        for (int i = 0; i < this.triggerHitboxes.size(); i++) {
            triggerHitboxes[i] = this.triggerHitboxes.get(i).getAABB();
        }
        return triggerHitboxes;
    }

    @Override
    public int getVRControllerNum() {
        return this.triggerControllerNum;
    }

    public Object getExtraData() {
        return this.extraData;
    }
}
