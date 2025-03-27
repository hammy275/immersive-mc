package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.client.ClientUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class BeaconInfo extends AbstractImmersiveInfo {

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
    public Vec3 effectSelectedDisplayPos = null;
    public boolean areaAboveIsAir = false;
    public long startMillis = 0;
    public int lastLevel = 0;
    public boolean levelWasNonzero = false;
    public int light = ClientUtil.maxLight;

    public BeaconInfo(BlockPos pos) {
        super(pos);
        for (int i = 0; i <= 8; i++) {
            hitboxes.add(new HitboxItemPair(null, ItemStack.EMPTY, i != 8));
        }
    }

    public boolean isEffectSelected() {
        return effectSelected > -1;
    }

    public boolean isReadyForConfirm() {
        HitboxItemPair pair = hitboxes.get(8);
        return isEffectSelected() && pair.item.is(ItemTags.BEACON_PAYMENT_ITEMS);
    }

    public int getEffectId() {
        return switch (this.effectSelected) {
            case 0 -> BuiltInRegistries.MOB_EFFECT.getId(MobEffects.SPEED.value());
            case 1 -> BuiltInRegistries.MOB_EFFECT.getId(MobEffects.HASTE.value());
            case 2 -> BuiltInRegistries.MOB_EFFECT.getId(MobEffects.RESISTANCE.value());
            case 3 -> BuiltInRegistries.MOB_EFFECT.getId(MobEffects.JUMP_BOOST.value());
            case 4 -> BuiltInRegistries.MOB_EFFECT.getId(MobEffects.STRENGTH.value());
            default -> -1;
        };
    }
}
