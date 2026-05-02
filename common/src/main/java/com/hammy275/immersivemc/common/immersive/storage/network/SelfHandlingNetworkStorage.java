package com.hammy275.immersivemc.common.immersive.storage.network;

import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import net.minecraft.server.level.ServerPlayer;

/**
 * A {@link NetworkStorage} that runs a handler when received by the other side.
 */
public interface SelfHandlingNetworkStorage extends NetworkStorage {

    void handleClient();

    void handleServer(ServerPlayer player);
}
