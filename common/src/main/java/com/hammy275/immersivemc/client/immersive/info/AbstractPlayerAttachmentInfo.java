package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractPlayerAttachmentInfo {

    protected int ticksLeft;
    protected int itemTransitionCountdown = 10; // Used for transitions for the items
    public int ticksActive = 0;
    public boolean initCompleted = false;
    protected BoundingBox[] inputHitboxes = null;
    public int light = ClientUtil.maxLight;
    public int slotHovered = -1;
    public int slotHovered2 = -1; // Only used in VR for secondary hand slot hovering.
    public int triggerHitboxSlotHovered = -1;

    public AbstractPlayerAttachmentInfo(int ticksToExist) {
        this.ticksLeft = ticksToExist;
    }

    public abstract void setInputSlots();

    public boolean slotHovered(int slot) {
        return slot == slotHovered || slot == slotHovered2;
    }
    /**
     * Gets all the slot IDs that represent inputs. Used for guiding.
     *
     * @return An array of all hitboxes that represent inputs
     */
    public BoundingBox[] getInputSlots() {
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

    public abstract BoundingBox getHitbox(int slot);

    public abstract BoundingBox[] getAllHitboxes();

    public abstract void setHitbox(int slot, BoundingBox hitbox);

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
