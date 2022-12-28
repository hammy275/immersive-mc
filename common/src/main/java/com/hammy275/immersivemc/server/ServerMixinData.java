package com.hammy275.immersivemc.server;

import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerMixinData {

    // For ServerPlayerGameModeMixin
    public static Map<UUID, BlockHitResult> results = new HashMap<>();
}
