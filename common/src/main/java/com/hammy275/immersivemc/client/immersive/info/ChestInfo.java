package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ChestShulkerOpenPacket;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ChestInfo extends AbstractBlockEntityImmersiveInfo<BlockEntity> {

    protected BoundingBox[] hitboxes = new BoundingBox[54];
    public BlockEntity other = null;
    public BlockPos otherPos = null;
    public Direction forward = null;
    public boolean failRender = false; // Used for thread safety when changing `other`
    protected int rowNum = 0;
    public boolean isOpen = false;
    public double lastY0;
    public double lastY1;
    public AABB[] openCloseHitboxes = new AABB[]{null, null};
    public Vec3[] openClosePositions = new Vec3[]{null, null};

    public ChestInfo(BlockEntity tileEntity, int ticksToExist, BlockEntity other) {
        super(tileEntity, ticksToExist, 53); // Accounts for double chest
        this.other = other;
        if (this.other != null) {
            this.otherPos = this.other.getBlockPos();
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
    public void setInputSlots() {
        if (this.isOpen) {
            this.inputHitboxes = this.hitboxes;
        } else {
            this.inputHitboxes = new BoundingBox[0];
        }

    }

    @Override
    public BoundingBox getHitbox(int slot) {
        return hitboxes[slot];
    }

    @Override
    public BoundingBox[] getAllHitboxes() {
        return hitboxes;
    }

    @Override
    public void setHitbox(int slot, BoundingBox hitbox) {
        hitboxes[slot] = hitbox;
    }

    @Override
    public boolean hasHitboxes() {
        return (hitboxes[8] != null || hitboxes[17] != null || hitboxes[26] != null);
    }

    @Override
    public boolean hasItems() {
        boolean mainChest = items[26] != null;
        boolean otherChest = this.other == null || items[53] != null;
        return mainChest && otherChest;
    }

    /**
     * If a double chest is broken, this function transforms the info representing a double chest to one representing
     * a single chest.
     * @return true on a successful transformation. false on failure (this was already a single chest or both chests are gone)
     */
    public boolean migrateToValidChest(Level level) {
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
    }
}
