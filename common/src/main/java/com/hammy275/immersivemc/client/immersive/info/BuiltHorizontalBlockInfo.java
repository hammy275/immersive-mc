package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.client.immersive.HitboxInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;

public class BuiltHorizontalBlockInfo extends BuiltImmersiveInfo {

    public Direction dir;

    public BuiltHorizontalBlockInfo(List<HitboxInfo> hitboxes, BlockPos pos, Direction dir, int ticksToExist) {
        super(hitboxes, pos, ticksToExist);
        this.dir = dir;
    }

    @Override
    public boolean readyToRender() {
        return super.readyToRender() && this.dir != null;
    }
}
