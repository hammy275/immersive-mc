package net.blf02.immersivemc.client.immersive.info;

import net.blf02.immersivemc.client.config.ClientConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.Arrays;

public class BackpackInfo extends AbstractImmersiveInfo {

    protected final Vector3d[] positions = new Vector3d[32];
    protected final AxisAlignedBB[] hitboxes = new AxisAlignedBB[32];
    // 0-26: Inventory
    // 27-31: Input crafting
    // 32: Output crafting

    public Vector3d handPos = Vector3d.ZERO;
    public Vector3d lookVec = Vector3d.ZERO;
    public Vector3d renderPos = Vector3d.ZERO;
    public Vector3d centerTopPos = Vector3d.ZERO;
    public Vector3d downVec = Vector3d.ZERO;
    public float handPitch = 0;
    public float handYaw = 0;
    public Vector3d backVec = Vector3d.ZERO;
    public Vector3f rgb = new Vector3f(0, 0, 0);
    public int topRow = 0;
    public int slotHovered = -1;
    public ItemStack[] craftingInput = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    public ItemStack craftingOutput = ItemStack.EMPTY;

    public BackpackInfo() {
        super(ClientConstants.ticksToRenderBackpack);
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes =
                Arrays.copyOfRange(hitboxes, 0, 31); // Disinclude last hitbox, since that's crafting output
    }

    @Override
    public AxisAlignedBB getHitbox(int slot) {
        return hitboxes[slot];
    }

    @Override
    public AxisAlignedBB[] getAllHitboxes() {
        return hitboxes;
    }

    @Override
    public void setHitbox(int slot, AxisAlignedBB hitbox) {
        hitboxes[slot] = hitbox;
    }

    @Override
    public boolean hasHitboxes() {
        return hitboxes[26] != null;
    }

    @Override
    public Vector3d getPosition(int slot) {
        return positions[slot];
    }

    @Override
    public Vector3d[] getAllPositions() {
        return positions;
    }

    @Override
    public void setPosition(int slot, Vector3d position) {
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
