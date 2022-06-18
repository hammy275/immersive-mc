package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Arrays;

public class AnvilInfo extends AbstractImmersiveInfo {

    public final BlockPos anvilPos;
    public Direction renderDirection;
    public boolean isReallyAnvil;

    protected AxisAlignedBB[] hitboxes = new AxisAlignedBB[3];
    protected Vector3d[] positions = new Vector3d[3];

    public Vector3d textPos = null;
    public Direction lastDir = null;

    public AnvilInfo(BlockPos pos, int ticksToExist) {
        super(ticksToExist);
        this.anvilPos = pos;
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = Arrays.copyOfRange(hitboxes, 0, 2);
    }

    @Override
    public AxisAlignedBB getHibtox(int slot) {
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
        return hitboxes[2] != null;
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
        return positions[2] != null && textPos != null;
    }

    @Override
    public boolean readyToRender() {
        return this.hasPositions() && this.hasHitboxes()
                 && this.renderDirection != null;
    }

    @Override
    public BlockPos getBlockPosition() {
        return anvilPos;
    }
}
