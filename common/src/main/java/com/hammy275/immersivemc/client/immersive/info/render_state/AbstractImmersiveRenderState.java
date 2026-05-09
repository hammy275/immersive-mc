package com.hammy275.immersivemc.client.immersive.info.render_state;

import com.hammy275.immersivemc.api.client.immersive.ImmersiveRenderState;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public abstract class AbstractImmersiveRenderState implements ImmersiveRenderState {

    public List<BoundingBox> hitboxes;
    public List<ItemStack> items;
    public BlockPos pos;
    public int[] slotsHovered;
    public long ticksExisted;

    public AbstractImmersiveRenderState() {

    }

    @Override
    public List<BoundingBox> hitboxes() {
        return hitboxes;
    }

    @Override
    public long ticksExisted() {
        return ticksExisted;
    }

    @Override
    public boolean isSlotHovered(int slot) {
        return slotsHovered[0] == slot || slotsHovered[1] == slot;
    }

    public boolean hasHitboxes() {
        return !hitboxes.isEmpty();
    }
}
