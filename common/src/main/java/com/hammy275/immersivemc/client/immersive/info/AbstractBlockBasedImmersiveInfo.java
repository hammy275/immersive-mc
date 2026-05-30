package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.client.immersive.BlockBasedImmersiveInfo;
import net.minecraft.core.BlockPos;

public class AbstractBlockBasedImmersiveInfo extends AbstractImmersiveInfo implements BlockBasedImmersiveInfo {

    protected final BlockPos pos;

    public AbstractBlockBasedImmersiveInfo(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public BlockPos getBlockPosition() {
        return pos;
    }
}
