package net.blf02.immersivemc.client.immersive.info;

import net.blf02.immersivemc.client.config.ClientConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class BackpackInfo extends AbstractImmersiveInfo {

    protected final Vector3d[] itemPositions = new Vector3d[27];
    protected final AxisAlignedBB[] itemHitboxes = new AxisAlignedBB[27];

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

    public BackpackInfo() {
        super(ClientConstants.ticksToRenderBackpack);
    }

    @Override
    public AxisAlignedBB getHibtox(int slot) {
        return itemHitboxes[slot];
    }

    @Override
    public AxisAlignedBB[] getAllHitboxes() {
        return itemHitboxes;
    }

    @Override
    public void setHitbox(int slot, AxisAlignedBB hitbox) {
        itemHitboxes[slot] = hitbox;
    }

    @Override
    public boolean hasHitboxes() {
        return itemHitboxes[26] != null;
    }

    @Override
    public Vector3d getPosition(int slot) {
        return itemPositions[slot];
    }

    @Override
    public Vector3d[] getAllPositions() {
        return itemPositions;
    }

    @Override
    public void setPosition(int slot, Vector3d position) {
        itemPositions[slot] = position;
    }

    @Override
    public boolean hasPositions() {
        return itemPositions[26] != null;
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
