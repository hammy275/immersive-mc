package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.util.math.AxisAlignedBB;

public abstract class AbstractImmersiveInfo {

    protected int ticksLeft;
    protected int countdown = 10; // Used for transitions for the items

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

    public int getCountdown() {
        return this.countdown;
    }

    public void changeCountdown(int amount) {
        this.countdown += amount;
    }
}
