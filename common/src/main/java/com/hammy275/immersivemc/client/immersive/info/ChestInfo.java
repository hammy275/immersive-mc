package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.client.ClientUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ChestInfo extends AbstractImmersiveInfo {

    public List<HitboxItemPair> hitboxes = new ArrayList<>(54);
    public BlockEntity chest;
    public BlockEntity otherChest;
    public BlockPos otherPos = null;
    public Direction forward = null;
    public boolean failRender = false; // Used for thread safety when changing `other`
    protected int rowNum = 0;
    public boolean isOpen = false;
    public double lastY0;
    public double lastY1;
    public AABB[] openCloseHitboxes = new AABB[]{null, null};
    public Vec3[] openClosePositions = new Vec3[]{null, null};
    public int light = ClientUtil.maxLight;

    public ChestInfo(BlockEntity chest, BlockEntity otherChest) {
        super(chest.getBlockPos()); // Accounts for double chest
        this.chest = chest;
        this.otherChest = otherChest;
        if (this.otherChest != null) {
            this.otherPos = this.otherChest.getBlockPos();
        }
        for (int i = 0; i < 54; i++) {
            hitboxes.add(new HitboxItemPair(null, ItemStack.EMPTY, false));
        }
    }

    public void nextRow() {
        rowNum = getNextRow(rowNum);
    }

    public int getNextRow(int rowIn) {
        if (++rowIn > 2) {
            return 0;
        }
        return rowIn;
    }

    public int getRowNum() {
        return rowNum;
    }

    @Override
    public List<HitboxItemPair> getAllHitboxes() {
        return hitboxes;
    }

    @Override
    public boolean hasHitboxes() {
        return (hitboxes.get(8).box != null || hitboxes.get(17).box != null || hitboxes.get(26).box != null) &&
                (this.otherChest == null || (hitboxes.get(35).box != null || hitboxes.get(44).box != null || hitboxes.get(53).box != null));
    }
}
