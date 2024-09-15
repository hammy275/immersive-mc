package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.client.config.ClientConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

public class BackpackInfo extends AbstractPlayerAttachmentInfo {

    protected final Vec3[] positions = new Vec3[32];
    protected final BoundingBox[] hitboxes = new BoundingBox[32];
    // 0-26: Inventory
    // 27-31: Input crafting
    // 32: Output crafting

    public Vec3 handPos = Vec3.ZERO;
    public Vec3 lookVec = Vec3.ZERO;
    public Vec3 renderPos = Vec3.ZERO;
    public Vec3 centerTopPos = Vec3.ZERO;
    public Vec3 downVec = Vec3.ZERO;
    public float handPitch = 0;
    public float handYaw = 0;
    public float handRoll = 0;
    public Vec3 backVec = Vec3.ZERO;
    public int argb = 0;
    public int topRow = 0;
    public ItemStack[] craftingInput = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    public ItemStack craftingOutput = ItemStack.EMPTY;
    public int light = -1;

    public BackpackInfo() {
        super(ClientConstants.ticksToRenderBackpack);
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes =
                Arrays.copyOfRange(hitboxes, 0, 31); // Disinclude last hitbox, since that's crafting output
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
        return hitboxes[26] != null;
    }

    @Override
    public Vec3 getPosition(int slot) {
        return positions[slot];
    }

    @Override
    public Vec3[] getAllPositions() {
        return positions;
    }

    @Override
    public void setPosition(int slot, Vec3 position) {
        positions[slot] = position;
    }

    @Override
    public boolean hasPositions() {
        return positions[26] != null;
    }

    @Override
    public boolean readyToRender() {
        return hasPositions() && hasHitboxes();
    }

    @Override
    public BlockPos getBlockPosition() {
        return Minecraft.getInstance().player.blockPosition();
    }

    public void gotoNextRow() {
        if (++this.topRow > 2) {
            this.topRow = 0;
        }
    }

    public int getMidRow() {
        int midRow = this.topRow + 1;
        if (midRow > 2) {
            return 0;
        }
        return midRow;
    }

    public int getBotRow() {
        int botRow = getMidRow() + 1;
        if (botRow > 2) {
            return 0;
        }
        return botRow;
    }
}
