package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.BuiltImmersiveInfo;
import com.hammy275.immersivemc.common.immersive.CheckerFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ImmersiveBuilder {

    // NOTE: Variables aren't prefixed with any visibility so they're package-private

    // -- Required --
    final CheckerFunction<BlockPos, BlockState, BlockEntity, Level, Boolean> blockChecker;

    // -- Optional --
    Supplier<Boolean> enabledInConfigSupplier = () -> true;
    int renderTime = ClientConstants.defaultTicksToRender;
    float renderSize = ClientConstants.defaultItemScaleSize;
    List<HitboxInfo> hitboxes = new ArrayList<>();
    List<Vec3i> lightPositionOffsets = new ArrayList<>();
    HitboxPositioningMode positioningMode = HitboxPositioningMode.HORIZONTAL_BLOCK_FACING;
    int maxImmersives = 4;
    Function<BuiltImmersiveInfo, Boolean> extraRenderReady = (info) -> true;
    RightClickHandler rightClickHandler = (a, b, c, d) -> {};

    private ImmersiveBuilder(CheckerFunction<BlockPos, BlockState, BlockEntity, Level, Boolean> blockChecker) {
        this.blockChecker = blockChecker;
    }

    /**
     * Sets the amount of time for the immersive to render.
     * @param time The amount of time for this immersive to render.
     * @return Builder object.
     */
    public ImmersiveBuilder setRenderTime(int time) {
        this.renderTime = time;
        return this;
    }

    /**
     * Sets the size for items rendered by this immersive.
     * @param size The size of the item when rendering.
     * @return Builder object.
     */
    public ImmersiveBuilder setRenderSize(float size) {
        this.renderSize = size;
        return this;
    }

    /**
     * Adds an item hitbox that can be interacted with and holds items. Note that these MUST be added in slot-order.
     * Aka, the underlying block's slot 0 should be added before its slot 1, etc.
     * @param centerOffset Offset from the center. X controls horizontal, Y controls vertical, and Z is going into/out
     *                     of the block. Offset 0,0,0 places the box at the center of the face that should be rendered.
     *                     This would be the front center of the block for the furnace, for example. Offsets are based
     *                     on the player, so for the furnace, -0.5, 0, 0 moves the box to the leftmost edge from the
     *                     player's perspective, or the rightmost edge if one were spectating the furnace.
     * @param size Size of the hitbox.
     * @param isInput Whether this is an input hitbox or not. Should set holdsItems to true if this is the case.
     * @param holdsItems Whether this hitbox holds items or not.
     * @return Builder object.
     */
    public ImmersiveBuilder addItemHitbox(Vec3 centerOffset, double size, boolean isInput, boolean holdsItems) {
        assert !isInput || holdsItems; // holdsItems should be true if isInput is.
        this.hitboxes.add(new HitboxInfo(centerOffset, size, isInput, holdsItems));
        return this;
    }

    /**
     * Sets the way hitboxes are positioned on the block.
     * @param newMode New mode for positioning.
     * @return Builder object.
     */
    public ImmersiveBuilder setPositioningMode(HitboxPositioningMode newMode) {
        this.positioningMode = newMode;
        return this;
    }

    /**
     * Adds a position to be used for light calculations. If none are supplied, a light position is determined
     * by the HitboxPositioningMode of the immersive.
     * @param lightPosOffset Offset of this block's position to add as a light source.
     * @return Builder object.
     */
    public ImmersiveBuilder addLightPositionOffset(Vec3i lightPosOffset) {
        this.lightPositionOffsets.add(lightPosOffset);
        return this;
    }

    /**
     * Sets a function used to retrieve the config value for whether this immerisve is enabled.
     * @param checker Checker to retrieve config value. Something such as () -> ActiveConfig.myConfigValue works here.
     * @return Builder object.
     */
    public ImmersiveBuilder setConfigChecker(Supplier<Boolean> checker) {
        this.enabledInConfigSupplier = checker;
        return this;
    }

    /**
     * Sets the maximum amount of infos this immersive should manage.
     * @param newMax The new maximum.
     * @return Builder object.
     */
    public ImmersiveBuilder setMaxImmersives(int newMax) {
        this.maxImmersives = newMax;
        return this;
    }

    /**
     * Sets an extra function to check if an info is ready to render. This is best used for things such as
     * air checking.
     * @param renderReady Function that consumes an info instance and returns if it's ready to render.
     * @return Builder object.
     */
    public ImmersiveBuilder setExtraRenderReady(Function<BuiltImmersiveInfo, Boolean> renderReady) {
        this.extraRenderReady = renderReady;
        return this;
    }

    /**
     * Sets what should happen on right click.
     * @param handler Function that handles a right-click.
     * @return Builder object.
     */
    public ImmersiveBuilder setRightClickHandler(RightClickHandler handler) {
        this.rightClickHandler = handler;
        return this;
    }

    public BuiltImmersive build() {
        return new BuiltImmersive(this);
    }

    public static ImmersiveBuilder create(CheckerFunction<BlockPos, BlockState, BlockEntity, Level, Boolean> blockChecker) {
        return new ImmersiveBuilder(blockChecker);
    }
}
