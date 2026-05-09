package com.hammy275.immersivemc.client.immersive.info.render_state;

import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import net.minecraft.core.Direction;

import java.util.List;

public class ChestRenderState extends AbstractImmersiveRenderState {

    public boolean hasOtherChest;
    public Direction forward;
    public int rowNum;
    public List<BoundingBox> openCloseHitboxes;
    public int light;
    public float openness;

}
