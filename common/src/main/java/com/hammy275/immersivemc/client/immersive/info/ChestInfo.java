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

public class ChestInfo extends AbstractImmersiveInfoV2 {

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

    /*
     * If a double chest is broken, this function transforms the info representing a double chest to one representing
     * a single chest.
     * @return true on a successful transformation. false on failure (this was already a single chest or both chests are gone)
     */
    /*public boolean migrateToValidChest(Level level) {
        boolean mainValid = getBlockPosition() != null && ImmersiveHandlers.chestHandler.isValidBlock(getBlockPosition(), level);
        boolean otherValid = otherPos != null && ImmersiveHandlers.chestHandler.isValidBlock(otherPos, level);
        if (!mainValid && !otherValid) {
            return false;
        }
        // Note that we don't migrate the items/positions/hitboxes, as that will be re-calculated later in
        // ImmersiveChest#doTick(). Just clear the ones for the other chest.
        boolean clearOther = false;
        boolean changeToOther = Minecraft.getInstance().level.getBlockEntity(this.otherPos) instanceof ChestBlockEntity other;
        if (mainValid) {
            // Other chest is invalid, remove references to it.
            clearOther = true;
        } else if (changeToOther) {
            // Check if actually a chest above in case a chest is next to something that became an ender chest or similar
            // Main chest is invalid. Migrate the other chest to be the main chest.
            this.tileEntity = other;
            if (isOpen) {
                // Close chests on break
                Network.INSTANCE.sendToServer(new ChestShulkerOpenPacket(this.pos, false));
                Network.INSTANCE.sendToServer(new ChestShulkerOpenPacket(other.getBlockPos(), isOpen));
            }
            this.pos = other.getBlockPos();
            clearOther = true;
        }
        if (clearOther) {
            this.other = null;
            this.otherPos = null;
            for (int i = 27; i < this.items.length; i++) {
                this.items[i] = null;
                this.hitboxes[i] = null;
                this.positions[i] = null;
            }
        }
        return clearOther;
    }*/
}
