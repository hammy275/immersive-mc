package com.hammy275.immersivemc.client.immersive.info.render_state;

import com.hammy275.immersivemc.api.client.immersive.ImmersiveRenderState;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.common.util.PosRot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class LecternRenderState implements ImmersiveRenderState {

    public ItemStack book;
    public final BookDataRenderState bookData = new BookDataRenderState();
    public PosRot posRot;
    public int light;
    public long ticksExisted;

    @Override
    public List<BoundingBox> hitboxes() {
        return bookData.obbs.stream().map(obb -> (BoundingBox) obb).toList();
    }

    @Override
    public long ticksExisted() {
        return ticksExisted;
    }

    @Override
    public boolean isSlotHovered(int slot) {
        return false;
    }
}
