package net.blf02.immersivemc.client.immersive.info;

import net.blf02.immersivemc.client.config.ClientConstants;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class RepeaterInfo extends AbstractImmersiveInfo {

    protected AxisAlignedBB[] hitboxes = new AxisAlignedBB[4];
    protected Vector3d[] positions = new Vector3d[4];
    protected final BlockPos pos;
    public boolean[] grabbedCurrent = new boolean[]{false, false}; // Index is for controller num

    public RepeaterInfo(BlockPos pos) {
        super(ClientConstants.ticksToRenderRepeater);
        this.pos = pos;
    }

    @Override
    public AxisAlignedBB getHibtox(int index) {
        return hitboxes[index];
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
        return hitboxes[3] != null;
    }

    @Override
    public Vector3d getPosition(int index) {
        return positions[index];
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
        return positions[3] != null;
    }

    @Override
    public boolean readyToRender() {
        return hasHitboxes() && hasPositions();
    }

    @Override
    public BlockPos getBlockPosition() {
        return pos;
    }
}
