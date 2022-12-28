package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractImmersiveInfo {

    protected int ticksLeft;
    protected int itemTransitionCountdown = 10; // Used for transitions for the items
    public int ticksActive = 0;
    public boolean initCompleted = false;
    protected AABB[] inputHitboxes = null;
    /*
    This variable is used in ONLY two spots:
        - By BackpackInfo to determine which hitbox is hovered over for trigger presses*
        - By desktop users to determine which hitbox is moused over
    Don't re-use this variable for other purposes unless you factor in the above! Mainly how that in VR,
    this variable will ONLY be updated for BackpackInfo instances.

    * - This was done because backpacks were implemented before the InfoTriggerHitboxes system.
     */
    public int slotHovered = -1;
    public int triggerHitboxSlotHovered = -1; // Same warnings as above

    public AbstractImmersiveInfo(int ticksToExist) {
        this.ticksLeft = ticksToExist;
    }

    public abstract void setInputSlots();

    /**
     * Gets all the slot IDs that represent inputs. Used for guiding.
     * @return An array of all hitboxes that represent inputs
     */
    public AABB[] getInputSlots() {
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

    public abstract AABB getHitbox(int slot);

    public abstract AABB[] getAllHitboxes();

    public abstract void setHitbox(int slot, AABB hitbox);

    public abstract boolean hasHitboxes();

    public abstract Vec3 getPosition(int slot);

    public abstract Vec3[] getAllPositions();

    public abstract void setPosition(int slot, Vec3 position);

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
