package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Info for Immersives that work by going to a hitbox, then dragging to other hitboxes, such as trapdoors.
 */
public class DragImmersiveInfo implements ImmersiveInfo {

    /**
     * The hitbox that needs to be started at to begin dragging, or -1 if one can start anywhere.
     */
    public int startingHitboxIndex;

    public final List<HitboxInfo> hitboxes = new ArrayList<>();
    public final int[] grabbedBox = new int[]{-1, -1};
    public int ticksExisted = 0;
    protected final BlockPos pos;

    public DragImmersiveInfo(BlockPos pos) {
        this(pos, -1);
    }

    public DragImmersiveInfo(BlockPos pos, int startingHitboxIndex) {
        this.pos = pos;
        this.startingHitboxIndex = startingHitboxIndex;
    }

    @Override
    public List<? extends HitboxInfo> getAllHitboxes() {
        return hitboxes;
    }

    @Override
    public boolean hasHitboxes() {
        return hitboxes.stream().anyMatch(hitbox -> hitbox != null && hitbox.getHitbox() != null);
    }

    @Override
    public BlockPos getBlockPosition() {
        return pos;
    }

    @Override
    public void setSlotHovered(int hitboxIndex, int handIndex) {
        // Intentional no-op.
    }

    @Override
    public int getSlotHovered(int handIndex) {
        return -1;
    }

    @Override
    public long getTicksExisted() {
        return ticksExisted;
    }
}
