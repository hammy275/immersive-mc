package com.hammy275.immersivemc.common.immersive.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

/**
 * Interface that can be attached to {@link com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler}
 * instances to perform some action after storage information is sent to the client.
 */
public interface AfterClientSyncHandler {

    void afterClientSync(ServerPlayer player, Set<BlockPos> positions);
}
