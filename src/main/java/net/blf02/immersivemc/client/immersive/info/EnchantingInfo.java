package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class EnchantingInfo extends AbstractImmersiveInfo {

    protected final Vector3d[] positions = new Vector3d[4];
    protected final AxisAlignedBB[] hitboxes = new AxisAlignedBB[4];
    protected final BlockPos tablePos;
    public boolean areaAboveIsAir = false;
    public final int[] yOffsetPositions = new int[]{0,
            ThreadLocalRandom.current().nextInt(10),
            ThreadLocalRandom.current().nextInt(15),
            ThreadLocalRandom.current().nextInt(20)};

    public int lookingAtIndex = -1;
    public Direction lastDir = null;

    public EnchantingInfo(BlockPos pos, int ticksToExist) {
        super(ticksToExist);
        this.tablePos = pos;
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = Arrays.copyOfRange(hitboxes, 0, 1);
    }

    @Override
    public AxisAlignedBB getHitbox(int slot) {
        return this.hitboxes[slot];
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
        return this.hitboxes[0] != null;
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
        return this.positions[3] != null;
    }

    @Override
    public boolean readyToRender() {
        return this.hasPositions() && this.hasHitboxes()
                 && areaAboveIsAir;
    }

    @Override
    public BlockPos getBlockPosition() {
        return this.tablePos;
    }
}
