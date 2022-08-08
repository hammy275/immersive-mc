package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractWorldStorageInfo extends AbstractImmersiveInfo {

    protected Vec3[] positions;
    protected AxisAlignedBB[] hitboxes;
    public ItemStack[] items;
    protected final BlockPos pos;
    public final int maxSlotIndex;

    public AbstractWorldStorageInfo(BlockPos pos, int ticksToExist, int maxSlotIndex) {
        super(ticksToExist);
        this.pos = pos;
        this.positions = new Vec3[maxSlotIndex + 1];
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
    public Vec3 getPosition(int slot) {
        return this.positions[slot];
    }

    @Override
    public Vec3[] getAllPositions() {
        return this.positions;
    }

    @Override
    public void setPosition(int slot, Vec3 position) {
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
