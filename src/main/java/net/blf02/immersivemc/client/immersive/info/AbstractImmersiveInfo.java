package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public abstract class AbstractImmersiveInfo {

    protected int ticksLeft;
    protected int itemTransitionCountdown = 10; // Used for transitions for the items
    public int ticksActive = 0;
    public boolean initCompleted = false;
    protected AxisAlignedBB[] inputHitboxes = null;
    public int ticksSinceLastClick = 0;

    public AbstractImmersiveInfo(int ticksToExist) {
        this.ticksLeft = ticksToExist;
    }

    public abstract void setInputSlots();

    /**
     * Gets all the slot IDs that represent inputs. Used for guiding.
     * @return An array of all hitboxes that represent inputs
     */
    public AxisAlignedBB[] getInputSlots() {
        return this.inputHitboxes;
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

    public abstract AxisAlignedBB getHitbox(int slot);

    public abstract AxisAlignedBB[] getAllHitboxes();

    public abstract void setHitbox(int slot, AxisAlignedBB hitbox);

    public abstract boolean hasHitboxes();

    public abstract Vector3d getPosition(int slot);

    public abstract Vector3d[] getAllPositions();

    public abstract void setPosition(int slot, Vector3d position);

    public abstract boolean hasPositions();

    public int getItemTransitionCountdown() {
        return this.itemTransitionCountdown;
    }

    public void changeItemTransitionCountdown(int amount) {
        this.itemTransitionCountdown += amount;
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
