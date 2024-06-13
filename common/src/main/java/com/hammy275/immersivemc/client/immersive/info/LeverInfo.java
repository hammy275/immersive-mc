package com.hammy275.immersivemc.client.immersive.info;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class LeverInfo extends AbstractImmersiveInfoV2 {

    protected List<HitboxItemPair> hitboxes = new ArrayList<>(2);
    protected Vec3[] positions = new Vec3[2];
    public int[] grabbedBox = new int[]{-1, -1}; // Index is for controller num

    public LeverInfo(BlockPos pos) {
        super(pos);
        for (int i = 0; i < 2; i++) {
            hitboxes.add(new HitboxItemPair(null, false));
        }
    }

    @Override
    public List<HitboxItemPair> getAllHitboxes() {
        return hitboxes;
    }

    @Override
    public boolean hasHitboxes() {
        return hitboxes.get(1).box != null;
    }
}
