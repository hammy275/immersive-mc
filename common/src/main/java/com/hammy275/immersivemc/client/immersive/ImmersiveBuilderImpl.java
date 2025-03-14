package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.immersive.*;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.MultiblockImmersiveHandler;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

public class ImmersiveBuilderImpl<E, S extends NetworkStorage> implements ImmersiveBuilder<E, S> {

    // NOTE: Variables aren't prefixed with any visibility, so they're package-private

    // -- Required --
    ImmersiveHandler<S> handler;

    // -- Optional --
    float renderSize = ClientConstants.defaultItemScaleSize;
    List<RelativeHitboxInfoImpl> hitboxes = new ArrayList<>();
    List<Vec3i> lightPositionOffsets = new ArrayList<>();
    HitboxPositioningMode positioningMode = HitboxPositioningMode.HORIZONTAL_BLOCK_FACING;
    Function<BuiltImmersiveInfo<E>, Boolean> extraRenderReady = (info) -> true;
    HitboxInteractHandler<E> hitboxInteractHandler = (a, b, c, d, e) -> -1;
    boolean vrOnly = false;
    List<Vec3i> airCheckPositionOffsets = new ArrayList<>();
    Class<E> extraInfoDataClazz;
    BiConsumer<S, BuiltImmersiveInfo<E>> extraStorageConsumer = null;
    BiFunction<BuiltImmersiveInfo<E>, Integer, Boolean> slotActive = null;
    Consumer<BuiltImmersiveInfo<E>> onRemove = (info) -> {};
    boolean blockRightClickWhenGUIClickDisabled = true;
    BiFunction<BuiltImmersiveInfo<E>, Integer, Boolean> slotRendersItemGuide = (info, slotNum) -> true;
    ImmersiveConfigScreenInfo configScreenInfo = null;
    ExtraRenderer<E> extraRenderer = (info, stack, helpers, partialTick, light) -> {};
    Function<BuiltImmersiveInfo<E>, AABB> dragHitboxCreator = null;

    public ImmersiveBuilderImpl(ImmersiveHandler<S> handler, @Nullable Class<E> extraInfoDataClazz) {
        this.handler = handler;
        this.extraInfoDataClazz = extraInfoDataClazz;
        if (handler instanceof MultiblockImmersiveHandler<?>) {
            throw new IllegalArgumentException("Cannot create an ImmersiveBuilder with a MultiblockImmersiveHandler.");
        }
    }


    /**
     * Sets the size for items rendered by this immersive.
     * @param size The size of the item when rendering.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<E,S> setRenderSize(float size) {
        this.renderSize = size;
        return this;
    }

    /**
     * Adds a hitbox. Note that item hitboxes MUST be added in slot-order.
     * Aka, the underlying block's slot 0 should be added before its slot 1, etc.
     * @param relativeHitboxInfo HitboxInfo to add. Can use HitboxInfoBuilder to make it easier to create.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<E,S> addHitbox(RelativeHitboxInfo relativeHitboxInfo) {
        this.hitboxes.add((RelativeHitboxInfoImpl) relativeHitboxInfo);
        return this;
    }

    /**
     * Adds a 3x3 grid of hitboxes, such as for the crafting table. Adds the top row from left to right,
     * then the middle row from left to right, then the bottom row from left to right.
     * @param relativeHitboxInfo HitboxInfo for center box. Can use HitboxInfoBuilder to make it easier to create.
     * @param distBetweenBoxes Distance between boxes.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<E,S> add3x3Grid(RelativeHitboxInfo relativeHitboxInfo, double distBetweenBoxes) {
        Vec3 left = new Vec3(-1, 0, 0).scale(distBetweenBoxes);
        Vec3 right = new Vec3(1, 0, 0).scale(distBetweenBoxes);
        Vec3 up = new Vec3(0, 1, 0).scale(distBetweenBoxes);
        Vec3 down = new Vec3(0, -1, 0).scale(distBetweenBoxes);
        Vec3 offset = new Vec3(0.0002, 0.0002, 0.0002); //Minor offset for each hitbox to prevent Z-fighting
        addHitbox(relativeHitboxInfo.cloneWithAddedOffset(up.add(left).add(offset)));
        addHitbox(relativeHitboxInfo.cloneWithAddedOffset(up.add(offset.scale(2))));
        addHitbox(relativeHitboxInfo.cloneWithAddedOffset(up.add(right).add(offset.scale(3))));
        addHitbox(relativeHitboxInfo.cloneWithAddedOffset(left.add(offset.scale(4))));
        addHitbox(relativeHitboxInfo.cloneWithAddedOffset(offset.scale(5)));
        addHitbox(relativeHitboxInfo.cloneWithAddedOffset(right.add(offset.scale(6))));
        addHitbox(relativeHitboxInfo.cloneWithAddedOffset(down.add(left).add(offset.scale(7))));
        addHitbox(relativeHitboxInfo.cloneWithAddedOffset(down.add(offset.scale(8))));
        addHitbox(relativeHitboxInfo.cloneWithAddedOffset(down.add(right).add(offset.scale(9))));
        return this;
    }

    /**
     * Sets the way hitboxes are positioned on the block.
     * @param newMode New mode for positioning.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<E,S> setPositioningMode(HitboxPositioningMode newMode) {
        this.positioningMode = newMode;
        return this;
    }

    /**
     * Sets what should happen when a hitbox is interacted with.
     * @param handler Function that takes an info instance, a player doing the interaction, the slot being interacted
     *                with, and the hand being interacted with. This function should return a number denoting the
     *                cooldown until the user can interact with Immersives again, or a negative number to denote that
     *                no interaction took place.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<E,S> setHitboxInteractHandler(HitboxInteractHandler<E> handler) {
        this.hitboxInteractHandler = handler;
        return this;
    }

    /**
     * Sets whether this immersive is only for VR users.
     * @param vrOnly Whether this immersive should now be VR only.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<E,S> setVROnly(boolean vrOnly) {
        this.vrOnly = vrOnly;
        return this;
    }

    /**
     * Sets a consumer that acts after an incoming NetworkStorage is parsed. For example, this is used
     * for the anvil to retrieve the level amount and store it in extra data.
     * @param storageConsumer New storage consumer.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<E,S> setExtraStorageConsumer(BiConsumer<S, BuiltImmersiveInfo<E>> storageConsumer) {
        this.extraStorageConsumer = storageConsumer;
        return this;
    }

    /**
     * Sets a function that determines whether a given slot should be active (rendered, reacts to interactions, etc.).
     * @param slotActive Function that takes an info instance and a slot number and returns whether the slot is active.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<E,S> setSlotActiveFunction(BiFunction<BuiltImmersiveInfo<E>, Integer, Boolean> slotActive) {
        this.slotActive = slotActive;
        return this;
    }

    /**
     * Set function to run on an info before it's removed.
     * @param onRemove Function to run on info just before removal.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<E,S> setOnRemove(Consumer<BuiltImmersiveInfo<E>> onRemove) {
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
    public ImmersiveBuilderImpl<E,S> shouldDisableRightClicksWhenInteractionsDisabled(boolean doDisable) {
        this.blockRightClickWhenGUIClickDisabled = doDisable;
        return this;
    }

    /**
     * Set whether the item guide for this slot should be active. This result is AND'd with the built-in checker,
     * which is simply if the slot holds items but currently isn't holding one.
     * @param itemGuideActive Function that returns whether the given slot is active given the info.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<E,S> setShouldRenderItemGuideFunction(BiFunction<BuiltImmersiveInfo<E>, Integer, Boolean> itemGuideActive) {
        this.slotRendersItemGuide = itemGuideActive;
        return this;
    }

    @Override
    public ImmersiveBuilder<E, S> setConfigScreenInfo(ImmersiveConfigScreenInfo info) {
        this.configScreenInfo = info;
        return this;
    }

    @Override
    public ImmersiveBuilder<E, S> setExtraRenderer(ExtraRenderer<E> renderer) {
        this.extraRenderer = renderer;
        return this;
    }

    /**
     * Overwrites hitbox at index with a new hitbox. Useful when cloning.
     * @param index Index to overwrite.
     * @param relativeHitboxInfo New hitbox information.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<E,S> overwriteHitbox(int index, RelativeHitboxInfo relativeHitboxInfo) {
        this.hitboxes.set(index, (RelativeHitboxInfoImpl) relativeHitboxInfo);
        return this;
    }

    /**
     * Modify a hitbox.
     * @param index Index of hitbox to modify.
     * @param modifier A function that takes the old hitbox as a builder and returns new hitbox info.
     * @return Builder object.
     */
    @Override
    public ImmersiveBuilderImpl<E,S> modifyHitbox(int index, Function<RelativeHitboxInfoBuilder, RelativeHitboxInfo> modifier) {
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
    public ImmersiveBuilderImpl<E,S> modifyHitboxes(int startIndex, int endIndex, Function<RelativeHitboxInfoBuilder, RelativeHitboxInfo> modifier) {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex || endIndex >= hitboxes.size()) {
            throw new IllegalArgumentException("Invalid starting and ending index. Keep them in range of the hitboxes, and make sure startIndex < endIndex.");
        }
        for (int i = startIndex; i <= endIndex; i++) {
            overwriteHitbox(i, modifier.apply(hitboxes.get(i).getBuilderClone()));
        }
        return this;
    }

    @Override
    public ImmersiveBuilder<E, S> setDragHitboxCreator(@Nullable Function<BuiltImmersiveInfo<E>, AABB> dragHitboxCreator) {
        this.dragHitboxCreator = dragHitboxCreator;
        return this;
    }

    public BuiltImmersiveImpl<E, S> build() {
        return new BuiltImmersiveImpl<>(this);
    }

    /**
     * Create a copy of this ImmersiveBuilder, setting the extra storage consumer to null.
     * @return A best-effort copy of this ImmersiveBuilder.
     */
    @Override
    public <T extends NetworkStorage> ImmersiveBuilderImpl<E, T> copy(ImmersiveHandler<T> newHandler) {
        ImmersiveBuilderImpl<E, T> clone = new ImmersiveBuilderImpl<>(newHandler, this.extraInfoDataClazz);
        clone.renderSize = this.renderSize;
        clone.hitboxes = new ArrayList<>(this.hitboxes);
        clone.lightPositionOffsets = new ArrayList<>(this.lightPositionOffsets);
        clone.positioningMode = this.positioningMode;
        clone.extraRenderReady = this.extraRenderReady;
        clone.hitboxInteractHandler = this.hitboxInteractHandler;
        clone.vrOnly = this.vrOnly;
        clone.airCheckPositionOffsets = new ArrayList<>(this.airCheckPositionOffsets);
        clone.extraStorageConsumer = null;
        clone.slotActive = this.slotActive;
        clone.onRemove = this.onRemove;
        clone.blockRightClickWhenGUIClickDisabled = this.blockRightClickWhenGUIClickDisabled;
        clone.slotRendersItemGuide = this.slotRendersItemGuide;
        clone.configScreenInfo = null;
        clone.extraRenderer = this.extraRenderer;
        clone.dragHitboxCreator = this.dragHitboxCreator;
        return clone;
    }

    @Override
    public <F, T extends NetworkStorage> ImmersiveBuilderImpl<F, T> copy(ImmersiveHandler<T> newHandler, Class<F> newExtraInfoDataClass) {
        ImmersiveBuilderImpl<F, T> clone = new ImmersiveBuilderImpl<>(newHandler, newExtraInfoDataClass);
        clone.renderSize = this.renderSize;
        clone.hitboxes = new ArrayList<>(this.hitboxes);
        clone.lightPositionOffsets = new ArrayList<>(this.lightPositionOffsets);
        clone.positioningMode = this.positioningMode;
        clone.extraRenderReady = (info) -> true;
        clone.hitboxInteractHandler = (a, b, c, d, modifierPressed) -> -1;
        clone.vrOnly = this.vrOnly;
        clone.airCheckPositionOffsets = new ArrayList<>(this.airCheckPositionOffsets);
        clone.extraStorageConsumer = null;
        clone.blockRightClickWhenGUIClickDisabled = this.blockRightClickWhenGUIClickDisabled;
        clone.configScreenInfo = null;
        return clone;
    }
}
