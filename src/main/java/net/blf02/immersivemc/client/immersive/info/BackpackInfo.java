package net.blf02.immersivemc.client.immersive.info;

import net.blf02.immersivemc.client.config.ClientConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class BackpackInfo extends AbstractImmersiveInfo {

    protected final Vector3d[] itemPositions = new Vector3d[27];
    protected final AxisAlignedBB[] itemHitboxes = new AxisAlignedBB[27];

    public Vector3d handPos = Vector3d.ZERO;
    public Vector3d lookVec = Vector3d.ZERO;
    public Vector3d renderPos = Vector3d.ZERO;
    public float handPitch = 0;
    public float handYaw = 0;
    public float handRoll = 0;
    public Vector3d backVec = Vector3d.ZERO;

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
        return true;
    }

    @Override
    public BlockPos getBlockPosition() {
        return Minecraft.getInstance().player.blockPosition();
    }
}
