package com.hammy275.immersivemc.client.immersive.info;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class RepeaterInfo extends AbstractImmersiveInfoV2 {

    protected List<HitboxItemPair> hitboxes = new ArrayList<>();
    public boolean[] grabbedCurrent = new boolean[]{false, false}; // Index is for controller num

    public RepeaterInfo(BlockPos pos) {
        super(pos);
        for (int i = 0; i < 4; i++) {
            hitboxes.add(new HitboxItemPair(null, false));
        }
    }

    @Override
    public List<HitboxItemPair> getAllHitboxes() {
        return hitboxes;
    }

    @Override
    public boolean hasHitboxes() {
        return hitboxes.get(3).box != null;
    }

}
