package com.hammy275.immersivemc.client.immersive;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class HitboxInfoBuilder {

    // -- REQUIRED --
    /**
     * Offset from the center. X controls horizontal, Y controls vertical, and Z is going into/out
     * of the block. Offset 0,0,0 places the box at the center of the face that should be rendered.
     * This would be the front center of the block for the furnace, for example. Offsets are based
     * on the player, so for the furnace, -0.5, 0, 0 moves the box to the leftmost edge from the
     * player's perspective, or the rightmost edge if one were spectating the furnace.
     */
    private final Vec3 centerOffset;
    /**
     * Size of the hitbox.
     */
    private final double size;

    // -- OPTIONAL --

    /**
     * Whether this hitbox holds items or not.
     */
    private boolean holdsItems = false;
    /**
     * Whether this is an item input hitbox or not. Should set holdsItems to true if this is the case.
     */
    private boolean isInput = false;
    /**
     * Forces item to be rendered facing up or down. Set to null to not do anything.
     */
    private Direction upDownRenderDir = null;
    /**
     * Makes item rendering spin.
     */
    private boolean itemSpins = false;
    /**
     * Multiplies item rendering size by this value.
     */
    private float itemRenderSizeMultiplier = 1f;


    private HitboxInfoBuilder(Vec3 centerOffset, double size) {
        this.centerOffset = centerOffset;
        this.size = size;
    }

    public HitboxInfoBuilder holdsItems(boolean holdsItems) {
        this.holdsItems = holdsItems;
        return this;
    }

    public HitboxInfoBuilder isInput(boolean isInput) {
        this.isInput = isInput;
        return this;
    }

    public HitboxInfoBuilder upDownRenderDir(Direction dir) {
        assert dir == null || dir == Direction.UP || dir == Direction.DOWN;
        this.upDownRenderDir = dir;
        return this;
    }

    public HitboxInfoBuilder itemSpins(boolean spins) {
        this.itemSpins = spins;
        return this;
    }

    public HitboxInfoBuilder itemRenderSizeMultiplier(float multiplier) {
        this.itemRenderSizeMultiplier = multiplier;
        return this;
    }

    public HitboxInfo build() {
        assert !isInput || holdsItems; // If isInput, must holdsItems
        return new HitboxInfo(centerOffset, size, holdsItems, isInput, upDownRenderDir,
                itemSpins, itemRenderSizeMultiplier);
    }

    public static HitboxInfoBuilder create(Vec3 centerOffset, double size) {
        return new HitboxInfoBuilder(centerOffset, size);
    }

    public static HitboxInfoBuilder createItemInput(Vec3 centerOffset, double size) {
        return create(centerOffset, size).holdsItems(true).isInput(true);
    }
}
