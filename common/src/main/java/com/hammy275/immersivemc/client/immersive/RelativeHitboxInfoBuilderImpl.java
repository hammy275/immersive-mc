package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.immersive.BuiltImmersiveInfo;
import com.hammy275.immersivemc.api.client.immersive.ForcedUpDownRenderDir;
import com.hammy275.immersivemc.api.client.immersive.HitboxVRMovementInfo;
import com.hammy275.immersivemc.api.client.immersive.ItemRotationType;
import com.hammy275.immersivemc.api.client.immersive.RelativeHitboxInfoBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class RelativeHitboxInfoBuilderImpl implements RelativeHitboxInfoBuilder, Cloneable {

    // -- REQUIRED --
    /**
     * Offset from the center. X controls horizontal, Y controls vertical, and Z is going into/out
     * of the block. Offset 0,0,0 places the box at the center of the face that should be rendered.
     * This would be the front center of the block for the furnace, for example. Offsets are based
     * on the player, so for the furnace, -0.5, 0, 0 moves the box to the leftmost edge from the
     * player's perspective, or the rightmost edge if one were spectating the furnace.
     */
    private Function<BuiltImmersiveInfo<?>, Vec3> centerOffset;
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
    private Function<BuiltImmersiveInfo<?>, List<Pair<Component, Vec3>>> textSupplier = null;
    /**
     * For hitboxes containing an item, this forces the item to render facing UP, DOWN, or null instead of
     * the default for the given HitboxPositioningMode.
     */
    private Function<BuiltImmersiveInfo<?>, ForcedUpDownRenderDir> forcedUpDown = ignored -> ForcedUpDownRenderDir.NOT_FORCED;
    /**
     * Whether the function above is constant
     */
    private boolean forcedUpDownConstant = true;
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
    /**
     * Whether to lerp when moving.
     */
    private boolean lerps = true;
    /**
     * A method of rotation to apply when rendering the item in a hitbox, or null to not rotate.
     */
    private ItemRotationType itemRotationType = null;

    // Automatically determined
    private final boolean constantOffset;


    public RelativeHitboxInfoBuilderImpl(Function<BuiltImmersiveInfo<?>, Vec3> centerOffset, double size, boolean constantOffset) {
        this(centerOffset, size, size, size, constantOffset);
    }

    public RelativeHitboxInfoBuilderImpl(Function<BuiltImmersiveInfo<?>, Vec3> centerOffset, double sizeX, double sizeY, double sizeZ,
                                          boolean constantOffset) {
        this.centerOffset = centerOffset;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.constantOffset = constantOffset;
    }

    public RelativeHitboxInfoBuilderImpl(Vec3 centerOffset, double size) {
        this((info) -> centerOffset, size, true);
    }

    public RelativeHitboxInfoBuilderImpl(Vec3 centerOffset, double sizeX, double sizeY, double sizeZ) {
        this((info) -> centerOffset, sizeX, sizeY, sizeZ, true);
    }

    public RelativeHitboxInfoBuilderImpl setCenterOffset(Function<BuiltImmersiveInfo<?>, Vec3> newOffset) {
        this.centerOffset = newOffset;
        return this;
    }

    public RelativeHitboxInfoBuilderImpl setCenterOffset(Vec3 newOffset) {
        this.centerOffset = (info) -> newOffset;
        return this;
    }

    public RelativeHitboxInfoBuilderImpl holdsItems(boolean holdsItems) {
        this.holdsItems = holdsItems;
        return this;
    }

    public RelativeHitboxInfoBuilderImpl isInput(boolean isInput) {
        this.isInput = isInput;
        return this;
    }

    public RelativeHitboxInfoBuilderImpl itemSpins(boolean spins) {
        this.itemSpins = spins;
        return this;
    }

    public RelativeHitboxInfoBuilderImpl itemRenderSizeMultiplier(float multiplier) {
        this.itemRenderSizeMultiplier = multiplier;
        return this;
    }

    public RelativeHitboxInfoBuilderImpl triggerHitbox(boolean isTriggerHitbox) {
        this.isTriggerHitbox = isTriggerHitbox;
        return this;
    }

    public RelativeHitboxInfoBuilderImpl textSupplier(Function<BuiltImmersiveInfo<?>, List<Pair<Component, Vec3>>> textSupplier) {
        this.textSupplier = textSupplier;
        return this;
    }

    public RelativeHitboxInfoBuilderImpl forceUpDownRenderDir(ForcedUpDownRenderDir forcedDir) {
        this.forcedUpDown = ignored -> forcedDir;
        this.forcedUpDownConstant = true;
        return this;
    }

    @Override
    public RelativeHitboxInfoBuilder forceUpDownRenderDir(Function<BuiltImmersiveInfo<?>, ForcedUpDownRenderDir> forcedDirFunction) {
        this.forcedUpDown = forcedDirFunction;
        this.forcedUpDownConstant = false;
        return this;
    }

    public RelativeHitboxInfoBuilderImpl needs3DResourcePackCompat(boolean needs3dCompat) {
        this.needs3dCompat = needs3dCompat;
        return this;
    }

    public RelativeHitboxInfoBuilderImpl setVRMovementInfo(HitboxVRMovementInfo vrMovementInfo) {
        assert vrMovementInfo.thresholds().length > 0;
        for (double threshold : vrMovementInfo.thresholds()) {
            assert threshold != 0;
        }
        if (vrMovementInfo.relativeAxis() == null) {
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

    public RelativeHitboxInfoBuilderImpl renderItem(boolean renderItem) {
        this.renderItem = renderItem;
        return this;
    }

    public RelativeHitboxInfoBuilderImpl renderItemCount(boolean renderItemCount) {
        this.renderItemCount = renderItemCount;
        return this;
    }

    @Override
    public RelativeHitboxInfoBuilder lerps(boolean lerps) {
        this.lerps = lerps;
        return this;
    }

    @Override
    public RelativeHitboxInfoBuilder rotateItem(@Nullable ItemRotationType itemRotationType) {
        this.itemRotationType = itemRotationType;
        return this;
    }

    public RelativeHitboxInfoImpl build() {
        assert !isInput || holdsItems; // If isInput, must holdsItems
        return new RelativeHitboxInfoImpl(this, centerOffset, sizeX, sizeY, sizeZ, holdsItems, isInput,
                itemSpins, itemRenderSizeMultiplier, isTriggerHitbox, textSupplier,
                forcedUpDown, constantOffset, needs3dCompat, vrMovementInfo, renderItem, renderItemCount, forcedUpDownConstant, lerps, itemRotationType);
    }

    public static RelativeHitboxInfoBuilderImpl create(Vec3 centerOffset, double size) {
        return new RelativeHitboxInfoBuilderImpl(centerOffset, size);
    }

    public static RelativeHitboxInfoBuilderImpl create(Function<BuiltImmersiveInfo<?>, Vec3> centerOffset, double size) {
        return new RelativeHitboxInfoBuilderImpl(centerOffset, size, false);
    }

    public static RelativeHitboxInfoBuilderImpl create(Vec3 centerOffset, double sizeX, double sizeY, double sizeZ) {
        return new RelativeHitboxInfoBuilderImpl(centerOffset, sizeX, sizeY, sizeZ);
    }

    public static RelativeHitboxInfoBuilderImpl create(Function<BuiltImmersiveInfo<?>, Vec3> centerOffset, double sizeX, double sizeY, double sizeZ) {
        return new RelativeHitboxInfoBuilderImpl(centerOffset, sizeX, sizeY, sizeZ, false);
    }

    public static RelativeHitboxInfoBuilderImpl createItemInput(Vec3 centerOffset, double size) {
        return new RelativeHitboxInfoBuilderImpl(centerOffset, size).holdsItems(true).isInput(true);
    }

    public static RelativeHitboxInfoBuilderImpl createItemInput(Function<BuiltImmersiveInfo<?>, Vec3> centerOffset, double size) {
        return new RelativeHitboxInfoBuilderImpl(centerOffset, size, false).holdsItems(true).isInput(true);
    }

    @Override
    public RelativeHitboxInfoBuilderImpl clone() {
        try {
            return (RelativeHitboxInfoBuilderImpl) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
