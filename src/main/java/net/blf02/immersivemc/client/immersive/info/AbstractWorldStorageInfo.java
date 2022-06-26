package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public abstract class AbstractWorldStorageInfo extends AbstractImmersiveInfo {

    protected Vector3d[] positions;
    protected AxisAlignedBB[] hitboxes;
    public ItemStack[] items;
    protected final BlockPos pos;
    public final int maxSlotIndex;

    public AbstractWorldStorageInfo(BlockPos pos, int ticksToExist, int maxSlotIndex) {
        super(ticksToExist);
        this.pos = pos;
        this.positions = new Vector3d[maxSlotIndex + 1];
        this.items = new ItemStack[maxSlotIndex + 1];
        this.hitboxes = new AxisAlignedBB[maxSlotIndex + 1];
        this.maxSlotIndex = maxSlotIndex;
    }

    @Override
    public AxisAlignedBB getHitbox(int slot) {
        return hitboxes[slot];
    }

    @Override
    public AxisAlignedBB[] getAllHitboxes() {
        return this.hitboxes;
    }

    @Override
    public void setHitbox(int slot, AxisAlignedBB hitbox) {
        this.hitboxes[slot] = hitbox;
    }

    @Override
    public boolean hasHitboxes() {
        return this.hitboxes[maxSlotIndex] != null;
    }

    @Override
    public Vector3d getPosition(int slot) {
        return this.positions[slot];
    }

    @Override
    public Vector3d[] getAllPositions() {
        return this.positions;
    }

    @Override
    public void setPosition(int slot, Vector3d position) {
        this.positions[slot] = position;
    }

    @Override
    public boolean hasPositions() {
        return this.positions[maxSlotIndex] != null;
    }

    @Override
    public boolean readyToRender() {
        return this.hasHitboxes() && this.hasPositions() && this.items[maxSlotIndex] != null;
    }

    @Override
    public BlockPos getBlockPosition() {
        return this.pos;
    }
}
