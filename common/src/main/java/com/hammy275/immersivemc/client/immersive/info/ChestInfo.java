package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ChestOpennessStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ChestInfo extends AbstractImmersiveInfo {

    public List<HitboxItemPair> hitboxes = new ArrayList<>(54);
    public BlockEntity chest;
    public BlockEntity otherChest;
    public BlockPos otherPos = null;
    public Direction forward = null;
    protected int rowNum = 0;
    public double lastY0;
    public double lastY1;
    public BoundingBox[] openCloseHitboxes = new BoundingBox[]{null, null};
    public Vec3[] openClosePositions = new Vec3[]{null, null};
    public int light = ClientUtil.maxLight;
    private ChestOpennessStorage opennessStorage = null;

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

    public boolean isOpen() {
        return getDirectOpenness() >= 0.1f;
    }

    public boolean slotVisible(int slot) {
        float openness = getDirectOpenness();
        if (slot % 9 >= 6) { // Slot is in the front row
            return openness >= 0.1f;
        } else if (slot % 9 >= 3) { // Slot is in the middle row
            return openness >= 0.3f;
        } else { // Slot is in the back row
            return openness >= 0.5f;
        }
    }

    public void setOpennessStorage(ChestOpennessStorage opennessStorage) {
        this.opennessStorage = opennessStorage;
    }

    public float getForcedOpenness() {
        return opennessStorage == null ? -1 : opennessStorage.getOpenness();
    }

    /**
     * Gets the openness from the chest, ignoring the forced openness. Mainly useful since the non-VR code path for
     * opening/closing chests doesn't modify forcedOpenness.
     * @return The openness of the chest as rendered in-world.
     */
    private float getDirectOpenness() {
        if (chest instanceof LidBlockEntity lbe) {
            return lbe.getOpenNess(1f);
        }
        return -1f;
    }
}
