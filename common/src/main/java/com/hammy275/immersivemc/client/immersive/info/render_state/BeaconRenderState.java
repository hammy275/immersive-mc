package com.hammy275.immersivemc.client.immersive.info.render_state;

import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.phys.Vec3;

public class BeaconRenderState extends AbstractImmersiveRenderState {

    public Direction lastPlayerDir;
    public boolean areaAboveIsAir;
    public int light;
    public int effectSelected;
    public Vec3 effectSelectedDisplayPos;
    public boolean regenSelected;

    public boolean isEffectSelected() {
        return effectSelected > -1;
    }

    public boolean isReadyForConfirm() {
        return isEffectSelected() && items.get(8).is(ItemTags.BEACON_PAYMENT_ITEMS);
    }
}
