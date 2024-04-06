package com.hammy275.immersivemc.server.immersive;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.*;

public class DirtyTracker {
    // Technically speaking, this marks dirty for a given position in all levels
    public static final Map<ResourceKey<Level>, Set<BlockPos>> dirtyPositions = new HashMap<>();

    public static boolean isDirty(Level level, BlockPos pos) {
        if (dirtyPositions.containsKey(level.dimension())) {
            return dirtyPositions.get(level.dimension()).contains(pos);
        } else {
            return false;
        }
    }

    public static void markDirty(Level level, BlockPos pos) {
        Set<BlockPos> positions = dirtyPositions.computeIfAbsent(level.dimension(), (key) -> new HashSet<>());
        positions.add(pos);
    }

    public static void unmarkAllDirty() {
        // Only clear values, since we'll likely use the keys next tick.
        dirtyPositions.forEach((key, value) -> value.clear());
    }
}
