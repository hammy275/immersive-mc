package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.immersive.info.BuiltImmersiveInfo;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public class HitboxInfoBuilder {

    // -- REQUIRED --
    /**
     * Offset from the center. X controls horizontal, Y controls vertical, and Z is going into/out
     * of the block. Offset 0,0,0 places the box at the center of the face that should be rendered.
     * This would be the front center of the block for the furnace, for example. Offsets are based
     * on the player, so for the furnace, -0.5, 0, 0 moves the box to the leftmost edge from the
     * player's perspective, or the rightmost edge if one were spectating the furnace.
     */
    private final Function<BuiltImmersiveInfo, Vec3> centerOffset;
    /**
     * Left/right size of the hitbox. See X axis for centerOffset.
     */
    private final double sizeX;
    /**
     * Up/down size of the hitbox. See Y axis for centerOffset.
     */
    private final double sizeY;
    /**
     * Forward/back size of the hitbox. See Z axis for centerOffset.
     */
    private final double sizeZ;

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
    /**
     * VR users need to press trigger (or whatever left-click is mapped to) to activate this hitbox.
     */
    private boolean isTriggerHitbox = false;
    /**
     * Supplier of text to render at the position of this hitbox, or null if this never renders text.
     * null can be returned instead of a Component to render no text.
     */
    private Function<BuiltImmersiveInfo, Component> textSupplier = null;


    private HitboxInfoBuilder(Function<BuiltImmersiveInfo, Vec3> centerOffset, double size) {
        this(centerOffset, size, size, size);
    }

    private HitboxInfoBuilder(Function<BuiltImmersiveInfo, Vec3> centerOffset, double sizeX, double sizeY, double sizeZ) {
        this.centerOffset = centerOffset;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
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

    public HitboxInfoBuilder triggerHitbox(boolean isTriggerHitbox) {
        this.isTriggerHitbox = isTriggerHitbox;
        return this;
    }

    public HitboxInfoBuilder textSupplier(Function<BuiltImmersiveInfo, Component> textSupplier) {
        this.textSupplier = textSupplier;
        return this;
    }

    public HitboxInfo build() {
        assert !isInput || holdsItems; // If isInput, must holdsItems
        return new HitboxInfo(centerOffset, sizeX, sizeY, sizeZ, holdsItems, isInput, upDownRenderDir,
                itemSpins, itemRenderSizeMultiplier, isTriggerHitbox, textSupplier);
    }

    public static HitboxInfoBuilder create(Vec3 centerOffset, double size) {
        return create((info) -> centerOffset, size);
    }

    public static HitboxInfoBuilder create(Function<BuiltImmersiveInfo, Vec3> centerOffset, double size) {
        return new HitboxInfoBuilder(centerOffset, size);
    }

    public static HitboxInfoBuilder create(Vec3 centerOffset, double sizeX, double sizeY, double sizeZ) {
        return create((info) -> centerOffset, sizeX, sizeY, sizeZ);
    }

    public static HitboxInfoBuilder create(Function<BuiltImmersiveInfo, Vec3> centerOffset, double sizeX, double sizeY, double sizeZ) {
        return new HitboxInfoBuilder(centerOffset, sizeX, sizeY, sizeZ);
    }

    public static HitboxInfoBuilder createItemInput(Vec3 centerOffset, double size) {
        return createItemInput((info) -> centerOffset, size);
    }

    public static HitboxInfoBuilder createItemInput(Function<BuiltImmersiveInfo, Vec3> centerOffset, double size) {
        return create(centerOffset, size).holdsItems(true).isInput(true);
    }
}
