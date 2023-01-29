package com.hammy275.immersivemc.server.data;

import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AboutToThrowData {

    public static Map<UUID, ThrowRecord> aboutToThrowMap = new HashMap<>();


    public record ThrowRecord(Vec3 velocity, Vec3 dir) {}
}
