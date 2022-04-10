package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public abstract class AbstractImmersiveInfo {

    protected int ticksLeft;
    protected int countdown = 10; // Used for transitions for the items
    public int ticksActive = 0;

    public AbstractImmersiveInfo(int ticksToExist) {
        this.ticksLeft = ticksToExist;
    }

    public int getTicksLeft() {
        return this.ticksLeft;
    }

    public void changeTicksLeft(int amount) {
        this.ticksLeft += amount;
    }

    public void setTicksLeft(int value) {
        this.ticksLeft = value;
    }

    public abstract AxisAlignedBB getHibtox(int slot);

    public abstract AxisAlignedBB[] getAllHitboxes();

    public abstract void setHitbox(int slot, AxisAlignedBB hitbox);

    public abstract boolean hasHitboxes();

    public abstract Vector3d getPosition(int slot);

    public abstract Vector3d[] getAllPositions();

    public abstract void setPosition(int slot, Vector3d position);

    public abstract boolean hasPositions();

    public int getCountdown() {
        return this.countdown;
    }

    public void changeCountdown(int amount) {
        this.countdown += amount;
    }

    public abstract boolean readyToRender();

    public abstract BlockPos getBlockPosition();

    public void remove() {
        this.ticksLeft = 0;
    }

    @Override
    public String toString() {
        return "[ImmersiveInfo type " + this.getClass().getName() + "] Block Position: " + getBlockPosition();
    }
}
