package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.common.obb.BoundingBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

public class BeaconInfo extends AbstractWorldStorageInfo implements InfoTriggerHitboxes {

    public boolean regenSelected = false;
    public int effectSelected = -1;
    public Direction lastPlayerDir = null;
    /**
     * Breakdown:
     * Index 0-4 hold the 5 effects. Can contain nulls!
     * Index 5 holds the regen hitbox. Can be null!
     * Index 6 holds the + hitbox. Can be null!
     * Index 7 holds the confirm hitbox. Can be null!
     */
    public AABB[] triggerBoxes = new AABB[8];
    public Vec3 effectSelectedDisplayPos = null;
    public boolean areaAboveIsAir = false;
    public long startMillis = 0;
    public int lastLevel = 0;
    public boolean levelWasNonzero = false;
    public BeaconInfo(BlockPos pos) {
        super(pos, ClientConstants.ticksToRenderBeacon, 0);
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = Arrays.copyOfRange(hitboxes, 0, 1);
    }

    @Override
    public boolean readyToRender() {
        return super.readyToRender() && lastPlayerDir != null
                && triggerBoxes[1] != null && effectSelectedDisplayPos != null
                && areaAboveIsAir;
    }

    @Override
    public BoundingBox getTriggerHitbox(int hitboxNum) {
        return triggerBoxes[hitboxNum];
    }

    @Override
    public BoundingBox[] getTriggerHitboxes() {
        return triggerBoxes;
    }

    @Override
    public int getVRControllerNum() {
        return 0;
    }

    public boolean isReadyForConfirmExceptPayment() {
        return effectSelected > -1;
    }

    public boolean isReadyForConfirm() {
        return isReadyForConfirmExceptPayment() && items[0] != null &&
                items[0].is(ItemTags.BEACON_PAYMENT_ITEMS);
    }

    public int getEffectId() {
        switch (this.effectSelected) {
            case 0:
                return BuiltInRegistries.MOB_EFFECT.getId(MobEffects.MOVEMENT_SPEED);
            case 1:
                return BuiltInRegistries.MOB_EFFECT.getId(MobEffects.DIG_SPEED);
            case 2:
                return BuiltInRegistries.MOB_EFFECT.getId(MobEffects.DAMAGE_RESISTANCE);
            case 3:
                return BuiltInRegistries.MOB_EFFECT.getId(MobEffects.JUMP);
            case 4:
                return BuiltInRegistries.MOB_EFFECT.getId(MobEffects.DAMAGE_BOOST);
            default:
                return -1;
        }
    }
}
