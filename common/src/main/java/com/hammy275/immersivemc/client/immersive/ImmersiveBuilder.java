package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.BuiltImmersiveInfo;
import com.hammy275.immersivemc.common.immersive.CheckerFunction;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
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
    boolean usesWorldStorage = false;
    int triggerHitboxControllerNum = 0;
    boolean vrOnly = false;
    List<Vec3i> airCheckPositionOffsets = new ArrayList<>();
    Class<?> extraInfoDataClazz = null;
    BiConsumer<ImmersiveStorage, BuiltImmersiveInfo> extraStorageConsumer = null;
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
     * Adds a hitbox. Note that item hitboxes MUST be added in slot-order.
     * Aka, the underlying block's slot 0 should be added before its slot 1, etc.
     * @param hitboxInfo HitboxInfo to add. Can use HitboxInfoBuilder to make it easier to create.
     * @return Builder object.
     */
    public ImmersiveBuilder addHitbox(HitboxInfo hitboxInfo) {
        this.hitboxes.add(hitboxInfo);
        return this;
    }

    /**
     * Adds a 3x3 horizontal grid of hitboxes, such as for the crafting table. Adds the top row from left to right,
     * then the middle row from left to right, then the bottom row from left to right.
     * @param hitboxInfo HitboxInfo for center box. Can use HitboxInfoBuilder to make it easier to create.
     * @param distBetweenBoxes Distance between boxes.
     * @return Builder object.
     */
    public ImmersiveBuilder add3x3HorizontalGrid(HitboxInfo hitboxInfo, double distBetweenBoxes) {
        Vec3 left = new Vec3(-1, 0, 0).scale(distBetweenBoxes);
        Vec3 right = new Vec3(1, 0, 0).scale(distBetweenBoxes);
        Vec3 up = new Vec3(0, 1, 0).scale(distBetweenBoxes);
        Vec3 down = new Vec3(0, -1, 0).scale(distBetweenBoxes);
        addHitbox(hitboxInfo.cloneWithOffset(up.add(left)));
        addHitbox(hitboxInfo.cloneWithOffset(up));
        addHitbox(hitboxInfo.cloneWithOffset(up.add(right)));
        addHitbox(hitboxInfo.cloneWithOffset(left));
        addHitbox(hitboxInfo);
        addHitbox(hitboxInfo.cloneWithOffset(right));
        addHitbox(hitboxInfo.cloneWithOffset(down.add(left)));
        addHitbox(hitboxInfo.cloneWithOffset(down));
        addHitbox(hitboxInfo.cloneWithOffset(down.add(right)));
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
     * Note: Offsets are currently literal, and are not translated in the same way hitboxes are.
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

    /**
     * Sets whether this immersive uses world storage.
     * @param usesWorldStorage New state of using world storage (false by default).
     * @return Builder object.
     */
    public ImmersiveBuilder setUsesWorldStorage(boolean usesWorldStorage) {
        this.usesWorldStorage = usesWorldStorage;
        return this;
    }

    /**
     * Sets the controller num that's used for checking for trigger hitboxes.
     * @param controllerNum Controller number.
     * @return Builder object.
     */
    public ImmersiveBuilder setTriggerHitboxControllerNum(int controllerNum) {
        assert controllerNum == 0 || controllerNum == 1;
        this.triggerHitboxControllerNum = controllerNum;
        return this;
    }

    /**
     * Sets whether this immersive is only for VR users.
     * @param vrOnly Whether this immersive should now be VR only.
     * @return Builder object.
     */
    public ImmersiveBuilder setVROnly(boolean vrOnly) {
        this.vrOnly = vrOnly;
        return this;
    }

    /**
     * Adds a position to be used for cancelling rendering if exposed to air. If none are supplied, a position is
     * determined by the HitboxPositioningMode of the immersive. This is calculated identically to the light position
     * offset.
     * Note that internally, air is not actually checked for! Instead, it checks if the position can be replaced,
     * such as air, water, and tall grass.
     * Note: Offsets are currently literal, and are not translated in the same way hitboxes are.
     * @param offset Offset of this block's position to add as an air check.
     * @return Builder object.
     */
    public ImmersiveBuilder addAirCheckPos(Vec3i offset) {
        this.airCheckPositionOffsets.add(offset);
        return this;
    }

    /**
     * Sets a class to be attached to individual info instances to store additional data, such as the anvil's
     * experience levels. Can be set to null (the default) to not create an extra data instance.
     * Note that the supplied class must have a constructor with no parameters.
     * @param clazz Class that represents extra data storage, or null to specify none.
     * @return Builder object.
     */
    public ImmersiveBuilder setExtraInfoDataClass(Class<?> clazz) {
        this.extraInfoDataClazz = clazz;
        return this;
    }

    /**
     * Sets a consumer that acts after an incoming ImmersiveStorage is parsed. For example, this is used
     * for the anvil to retrieve the level amount and store it in extra data.
     * Note that the immersive MUST use world storage (call setUsesWorldStorage(true))
     * @param storageConsumer New storage consumer.
     * @return Builder object.
     */
    public ImmersiveBuilder setExtraStorageConsumer(BiConsumer<ImmersiveStorage, BuiltImmersiveInfo> storageConsumer) {
        this.extraStorageConsumer = storageConsumer;
        return this;
    }

    public BuiltImmersive build() {
        // Only allow extraStorageConsumer if we use world storage
        assert this.extraStorageConsumer == null || this.usesWorldStorage;
        return new BuiltImmersive(this);
    }

    public static ImmersiveBuilder create(CheckerFunction<BlockPos, BlockState, BlockEntity, Level, Boolean> blockChecker) {
        return new ImmersiveBuilder(blockChecker);
    }
}
