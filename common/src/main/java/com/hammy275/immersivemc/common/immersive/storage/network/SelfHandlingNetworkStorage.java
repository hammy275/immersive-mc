package com.hammy275.immersivemc.common.immersive.storage.network;

import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;

/**
 * A {@link NetworkStorage} that runs a handler when received by the client.
 */
public interface SelfHandlingNetworkStorage extends NetworkStorage {

    void handleClient();
}
