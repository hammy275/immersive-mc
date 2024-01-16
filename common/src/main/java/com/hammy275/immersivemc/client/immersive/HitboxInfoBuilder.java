package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.immersive.info.BuiltImmersiveInfo;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Function;

public class HitboxInfoBuilder implements Cloneable {

    // -- REQUIRED --
    /**
     * Offset from the center. X controls horizontal, Y controls vertical, and Z is going into/out
     * of the block. Offset 0,0,0 places the box at the center of the face that should be rendered.
     * This would be the front center of the block for the furnace, for example. Offsets are based
     * on the player, so for the furnace, -0.5, 0, 0 moves the box to the leftmost edge from the
     * player's perspective, or the rightmost edge if one were spectating the furnace.
     */
    private Function<BuiltImmersiveInfo, Vec3> centerOffset;
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
     * Supplier of text to render at the position of this hitbox, offset by the Vec3. Null if this never renders text.
     * null can be returned instead of a Pair to render no text. The Component and Vec3 must both be non-null if a
     * non-null Pair is returned. The list itself that is returned can also be null to render no text.
     * The Vec3 is run through the usual relative math in the same way the centerOffset is, but the position
     * is offset from centerOffset.
     */
    private Function<BuiltImmersiveInfo, List<Pair<Component, Vec3>>> textSupplier = null;
    /**
     * For hitboxes containing an item, this forces the item to render facing UP, DOWN, or null instead of
     * the default for the given HiboxPositioningMode.
     */
    private ForcedUpDownRenderDir forcedUpDown = ForcedUpDownRenderDir.NOT_FORCED;
    /**
     * If true, this hitbox is offset for 3D Resource Pack compatibility.
     */
    private boolean needs3dCompat = false;
    /**
     * If not null, the relative axis (any axis if null) and thresholds that causes the action callback to be performed.
     * There can be multiple thresholds in case a positive and negative one are wanted, and the controller mode should
     * be the wanted controller, both controllers, or either controller.
     */
    private HitboxVRMovementInfo vrMovementInfo = null;
    /**
     * Whether to render the item for this hitbox if it can contain items.
     */
    private boolean renderItem = true;
    /**
     * Whether to render the item count for this hitbox if it can contain items.
     */
    private boolean renderItemCount = true;

    // Automatically determined
    private final boolean constantOffset;


    private HitboxInfoBuilder(Function<BuiltImmersiveInfo, Vec3> centerOffset, double size, boolean constantOffset) {
        this(centerOffset, size, size, size, constantOffset);
    }

    private HitboxInfoBuilder(Function<BuiltImmersiveInfo, Vec3> centerOffset, double sizeX, double sizeY, double sizeZ,
                              boolean constantOffset) {
        this.centerOffset = centerOffset;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.constantOffset = constantOffset;
    }

    private HitboxInfoBuilder(Vec3 centerOffset, double size) {
        this((info) -> centerOffset, size, true);
    }

    private HitboxInfoBuilder(Vec3 centerOffset, double sizeX, double sizeY, double sizeZ) {
        this((info) -> centerOffset, sizeX, sizeY, sizeZ, true);
    }

    public HitboxInfoBuilder setCenterOffset(Function<BuiltImmersiveInfo, Vec3> newOffset) {
        this.centerOffset = newOffset;
        return this;
    }

    public HitboxInfoBuilder setCenterOffset(Vec3 newOffset) {
        this.centerOffset = (info) -> newOffset;
        return this;
    }

    public HitboxInfoBuilder holdsItems(boolean holdsItems) {
        this.holdsItems = holdsItems;
        return this;
    }

    public HitboxInfoBuilder isInput(boolean isInput) {
        this.isInput = isInput;
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

    public HitboxInfoBuilder textSupplier(Function<BuiltImmersiveInfo, List<Pair<Component, Vec3>>> textSupplier) {
        this.textSupplier = textSupplier;
        return this;
    }

    public HitboxInfoBuilder forceUpDownRenderDir(ForcedUpDownRenderDir forcedDir) {
        this.forcedUpDown = forcedDir;
        return this;
    }

    public HitboxInfoBuilder needs3DResourcePackCompat(boolean needs3dCompat) {
        this.needs3dCompat = needs3dCompat;
        return this;
    }

    public HitboxInfoBuilder setVRMovementInfo(HitboxVRMovementInfo vrMovementInfo) {
        assert vrMovementInfo.thresholds().length > 0;
        for (double threshold : vrMovementInfo.thresholds()) {
            assert threshold != 0;
        }
        if (vrMovementInfo.controllerMode() == null) {
            assert vrMovementInfo.thresholds().length == 1; // Only one threshold if axis doesn't matter.
        } else { // No more than 2 thresholds, and one threshold is positive and one negative.
            assert vrMovementInfo.thresholds().length == 1 ||
                    (vrMovementInfo.thresholds().length == 2 &&
                            Math.min(vrMovementInfo.thresholds()[0], vrMovementInfo.thresholds()[1]) < 0
                            && Math.max(vrMovementInfo.thresholds()[0], vrMovementInfo.thresholds()[1]) > 0);
        }
        this.vrMovementInfo = vrMovementInfo;
        return this;
    }

    public HitboxInfoBuilder renderItem(boolean renderItem) {
        this.renderItem = renderItem;
        return this;
    }

    public HitboxInfoBuilder renderItemCount(boolean renderItemCount) {
        this.renderItemCount = renderItemCount;
        return this;
    }

    public HitboxInfo build() {
        assert !isInput || holdsItems; // If isInput, must holdsItems
        return new HitboxInfo(this, centerOffset, sizeX, sizeY, sizeZ, holdsItems, isInput,
                itemSpins, itemRenderSizeMultiplier, isTriggerHitbox, textSupplier,
                forcedUpDown, constantOffset, needs3dCompat, vrMovementInfo, renderItem, renderItemCount);
    }

    public static HitboxInfoBuilder create(Vec3 centerOffset, double size) {
        return new HitboxInfoBuilder(centerOffset, size);
    }

    public static HitboxInfoBuilder create(Function<BuiltImmersiveInfo, Vec3> centerOffset, double size) {
        return new HitboxInfoBuilder(centerOffset, size, false);
    }

    public static HitboxInfoBuilder create(Vec3 centerOffset, double sizeX, double sizeY, double sizeZ) {
        return new HitboxInfoBuilder(centerOffset, sizeX, sizeY, sizeZ);
    }

    public static HitboxInfoBuilder create(Function<BuiltImmersiveInfo, Vec3> centerOffset, double sizeX, double sizeY, double sizeZ) {
        return new HitboxInfoBuilder(centerOffset, sizeX, sizeY, sizeZ, false);
    }

    public static HitboxInfoBuilder createItemInput(Vec3 centerOffset, double size) {
        return new HitboxInfoBuilder(centerOffset, size).holdsItems(true).isInput(true);
    }

    public static HitboxInfoBuilder createItemInput(Function<BuiltImmersiveInfo, Vec3> centerOffset, double size) {
        return new HitboxInfoBuilder(centerOffset, size, false).holdsItems(true).isInput(true);
    }

    @Override
    public HitboxInfoBuilder clone() {
        try {
            return (HitboxInfoBuilder) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
