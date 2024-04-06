package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public abstract class ContainerHandler implements ImmersiveHandler {

    @Override
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        return DirtyTracker.isDirty(player.level(), pos);
    }

}
