package net.blf02.immersivemc.client.immersive.info;

import net.blf02.immersivemc.client.config.ClientConstants;
import net.minecraft.util.math.AABB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.phys.Vec3;

public class RepeaterInfo extends AbstractImmersiveInfo {

    protected AABB[] hitboxes = new AABB[4];
    protected Vec3[] positions = new Vec3[4];
    protected final BlockPos pos;
    public boolean[] grabbedCurrent = new boolean[]{false, false}; // Index is for controller num

    public RepeaterInfo(BlockPos pos) {
        super(ClientConstants.ticksToRenderRepeater);
        this.pos = pos;
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = hitboxes;
    }

    @Override
    public AABB getHitbox(int index) {
        return hitboxes[index];
    }

    @Override
    public AABB[] getAllHitboxes() {
        return hitboxes;
    }

    @Override
    public void setHitbox(int slot, AABB hitbox) {
        hitboxes[slot] = hitbox;
    }

    @Override
    public boolean hasHitboxes() {
        return hitboxes[3] != null;
    }

    @Override
    public Vec3 getPosition(int index) {
        return positions[index];
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
