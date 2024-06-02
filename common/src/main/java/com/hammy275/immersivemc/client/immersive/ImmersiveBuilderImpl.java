package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.immersive.HitboxPositioningMode;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveBuilder;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.BuiltImmersiveInfo;
import com.hammy275.immersivemc.common.immersive.storage.network.NetworkStorage;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

public class ImmersiveBuilderImpl<S extends NetworkStorage> implements ImmersiveBuilder<S> {

    // NOTE: Variables aren't prefixed with any visibility, so they're package-private

    public static final BiFunction<BuiltImmersiveInfo, Integer, Boolean> SLOT_ALWAYS_ACTIVE = (info, slotNum) -> true;


    // -- Required --
    ImmersiveHandler<S> handler;

    // -- Optional --
    Supplier<Boolean> enabledInConfigSupplier = () -> true;
    int renderTime = ClientConstants.defaultTicksToRender;
    float renderSize = ClientConstants.defaultItemScaleSize;
    List<HitboxInfo> hitboxes = new ArrayList<>();
    List<Vec3i> lightPositionOffsets = new ArrayList<>();
    HitboxPositioningMode positioningMode = HitboxPositioningMode.HORIZONTAL_BLOCK_FACING;
    int maxImmersives = -1;
    Function<BuiltImmersiveInfo, Boolean> extraRenderReady = (info) -> true;
    RightClickHandler rightClickHandler = (a, b, c, d) -> {};
    int triggerHitboxControllerNum = 0;
    boolean vrOnly = false;
    List<Vec3i> airCheckPositionOffsets = new ArrayList<>();
    Class<?> extraInfoDataClazz = null;
    BiConsumer<S, BuiltImmersiveInfo> extraStorageConsumer = null;
    BiFunction<BuiltImmersiveInfo, Integer, Boolean> slotActive = SLOT_ALWAYS_ACTIVE;
    Consumer<BuiltImmersiveInfo> onRemove = (info) -> {};
    boolean blockRightClickWhenGUIClickDisabled = true;
    BiFunction<BuiltImmersiveInfo, Integer, Boolean> slotRendersItemGuide = (info, slotNum) -> true;

    public ImmersiveBuilderImpl(ImmersiveHandler<S> handler) {
        this.handler = handler;
    }


    /**
     * Sets the amount of time for the immersive to render.
     * @param time The amount of time for this immersive to render.
     * @return Builder object.
     */
    public ImmersiveBuilderImpl<S> setRenderTime(int time) {
        this.renderTime = time;
        return this;
    }

    /**
     * Sets the size for items rendered by this immersive.
     * @param size The size of the item when rendering.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<S> setRenderSize(float size) {
        this.renderSize = size;
        return this;
    }

    /**
     * Adds a hitbox. Note that item hitboxes MUST be added in slot-order.
     * Aka, the underlying block's slot 0 should be added before its slot 1, etc.
     * @param hitboxInfo HitboxInfo to add. Can use HitboxInfoBuilder to make it easier to create.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<S> addHitbox(HitboxInfo hitboxInfo) {
        this.hitboxes.add(hitboxInfo);
        return this;
    }

    /**
     * Adds a 3x3 grid of hitboxes, such as for the crafting table. Adds the top row from left to right,
     * then the middle row from left to right, then the bottom row from left to right.
     * @param hitboxInfo HitboxInfo for center box. Can use HitboxInfoBuilder to make it easier to create.
     * @param distBetweenBoxes Distance between boxes.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<S> add3x3Grid(HitboxInfo hitboxInfo, double distBetweenBoxes) {
        Vec3 left = new Vec3(-1, 0, 0).scale(distBetweenBoxes);
        Vec3 right = new Vec3(1, 0, 0).scale(distBetweenBoxes);
        Vec3 up = new Vec3(0, 1, 0).scale(distBetweenBoxes);
        Vec3 down = new Vec3(0, -1, 0).scale(distBetweenBoxes);
        addHitbox(hitboxInfo.cloneWithAddedOffset(up.add(left)));
        addHitbox(hitboxInfo.cloneWithAddedOffset(up));
        addHitbox(hitboxInfo.cloneWithAddedOffset(up.add(right)));
        addHitbox(hitboxInfo.cloneWithAddedOffset(left));
        addHitbox(hitboxInfo);
        addHitbox(hitboxInfo.cloneWithAddedOffset(right));
        addHitbox(hitboxInfo.cloneWithAddedOffset(down.add(left)));
        addHitbox(hitboxInfo.cloneWithAddedOffset(down));
        addHitbox(hitboxInfo.cloneWithAddedOffset(down.add(right)));
        return this;
    }

    /**
     * Sets the way hitboxes are positioned on the block.
     * @param newMode New mode for positioning.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<S> setPositioningMode(HitboxPositioningMode newMode) {
        this.positioningMode = newMode;
        return this;
    }

    /**
     * Sets a function used to retrieve the config value for whether this immerisve is enabled.
     * @param checker Checker to retrieve config value. Something such as () -> ActiveConfig.active().myConfigValue works here.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<S> setConfigChecker(Supplier<Boolean> checker) {
        this.enabledInConfigSupplier = checker;
        return this;
    }

    /**
     * Sets what should happen on right click.
     * @param handler Function that handles a right-click.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<S> setRightClickHandler(RightClickHandler handler) {
        this.rightClickHandler = handler;
        return this;
    }

    /**
     * Sets the controller num that's used for checking for trigger hitboxes.
     * @param controllerNum Controller number.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<S> setTriggerHitboxControllerNum(int controllerNum) {
        assert controllerNum == 0 || controllerNum == 1;
        this.triggerHitboxControllerNum = controllerNum;
        return this;
    }

    /**
     * Sets whether this immersive is only for VR users.
     * @param vrOnly Whether this immersive should now be VR only.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<S> setVROnly(boolean vrOnly) {
        this.vrOnly = vrOnly;
        return this;
    }

    /**
     * Sets a class to be attached to individual info instances to store additional data, such as the anvil's
     * experience levels. Can be set to null (the default) to not create an extra data instance.
     * Note that the supplied class must have a constructor with no parameters.
     * @param clazz Class that represents extra data storage, or null to specify none.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<S> setExtraInfoDataClass(Class<?> clazz) {
        this.extraInfoDataClazz = clazz;
        return this;
    }

    /**
     * Sets a consumer that acts after an incoming NetworkStorage is parsed. For example, this is used
     * for the anvil to retrieve the level amount and store it in extra data.
     * @param storageConsumer New storage consumer.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<S> setExtraStorageConsumer(BiConsumer<S, BuiltImmersiveInfo> storageConsumer) {
        this.extraStorageConsumer = storageConsumer;
        return this;
    }

    /**
     * Sets a function that determines whether a given slot should be active (rendered, reacts to interactions, etc.).
     * @param slotActive Function that takes an info instance and a slot number and returns whether the slot is active.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<S> setSlotActiveFunction(BiFunction<BuiltImmersiveInfo, Integer, Boolean> slotActive) {
        this.slotActive = slotActive;
        return this;
    }

    /**
     * Set function to run on an info before it's removed.
     * @param onRemove Function to run on info just before removal.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<S> setOnRemove(Consumer<BuiltImmersiveInfo> onRemove) {
        this.onRemove = onRemove;
        return this;
    }

    /**
     * Set whether to disable right-click interactions on this immersive when the option to disable said
     * interactions is enabled.
     * @param doDisable Whether to disable as described above.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<S> shouldDisableRightClicksWhenInteractionsDisabled(boolean doDisable) {
        this.blockRightClickWhenGUIClickDisabled = doDisable;
        return this;
    }

    /**
     * Set whether the item guide for this slot should be active. This result is &&'d with the built-in checker,
     * which is simply if the slot holds items but currently isn't holding one.
     * @param itemGuideActive Function that returns whether the given slot is active given the info.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<S> setShouldRenderItemGuideFunction(BiFunction<BuiltImmersiveInfo, Integer, Boolean> itemGuideActive) {
        this.slotRendersItemGuide = itemGuideActive;
        return this;
    }

    /**
     * Overwrites hitbox at index with a new hitbox. Useful when cloning.
     * @param index Index to overwrite.
     * @param hitboxInfo New hitbox information.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<S> overwriteHitbox(int index, HitboxInfo hitboxInfo) {
        this.hitboxes.set(index, hitboxInfo);
        return this;
    }

    /**
     * Modify a hitbox.
     * @param index Index of hitbox to modify.
     * @param modifier A function that takes the old hitbox as a builder and returns new hitbox info.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<S> modifyHitbox(int index, Function<HitboxInfoBuilder, HitboxInfo> modifier) {
        return modifyHitboxes(index, index, modifier);
    }

    /**
     * Modify a range of hitboxes, inclusive for both ends.
     * @param startIndex Starting index of range of hitboxes to modify inclusive.
     * @param endIndex Ending index of range of hitboxes to modify inclusive.
     * @param modifier A function that takes the old hitbox as a builder and returns new hitbox info.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<S> modifyHitboxes(int startIndex, int endIndex, Function<HitboxInfoBuilder, HitboxInfo> modifier) {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex || endIndex >= hitboxes.size()) {
            throw new IllegalArgumentException("Invalid starting and ending index. Keep them in range of the hitboxes, and make sure startIndex < endIndex.");
        }
        for (int i = startIndex; i <= endIndex; i++) {
            overwriteHitbox(i, modifier.apply(hitboxes.get(i).getBuilderClone()));
        }
        return this;
    }

    public BuiltImmersive<S> build() {
        return new BuiltImmersive<>(this);
    }

    public static <NS extends NetworkStorage> ImmersiveBuilderImpl<NS> create(ImmersiveHandler<NS> handler) {
        return new ImmersiveBuilderImpl<>(handler);
    }

    /**
     * Create a copy of this ImmersiveBuilder, setting the extra storage consumer to null.
     * @return A best-effort copy of this ImmersiveBuilder.
     */
    @Override
    public <T extends NetworkStorage> ImmersiveBuilderImpl<T> copy(ImmersiveHandler<T> newHandler) {
        ImmersiveBuilderImpl<T> clone = new ImmersiveBuilderImpl<>(newHandler);
        clone.enabledInConfigSupplier = this.enabledInConfigSupplier;
        clone.renderTime = this.renderTime;
        clone.renderSize = this.renderSize;
        clone.hitboxes = new ArrayList<>(this.hitboxes);
        clone.lightPositionOffsets = new ArrayList<>(this.lightPositionOffsets);
        clone.positioningMode = this.positioningMode;
        clone.maxImmersives = this.maxImmersives;
        clone.extraRenderReady = this.extraRenderReady;
        clone.rightClickHandler = this.rightClickHandler;
        clone.triggerHitboxControllerNum = this.triggerHitboxControllerNum;
        clone.vrOnly = this.vrOnly;
        clone.airCheckPositionOffsets = new ArrayList<>(this.airCheckPositionOffsets);
        clone.extraInfoDataClazz = this.extraInfoDataClazz;
        clone.extraStorageConsumer = null;
        clone.slotActive = this.slotActive;
        clone.onRemove = this.onRemove;
        clone.blockRightClickWhenGUIClickDisabled = this.blockRightClickWhenGUIClickDisabled;
        clone.slotRendersItemGuide = this.slotRendersItemGuide;
        return clone;
    }
}
