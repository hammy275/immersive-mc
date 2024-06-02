package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.immersive.*;
import com.hammy275.immersivemc.client.immersive.info.BuiltImmersiveInfo;
import com.hammy275.immersivemc.common.immersive.storage.network.NetworkStorage;

import java.util.function.*;

public interface ImmersiveBuilder {

    public static ImmersiveBuilder create(ImmersiveHandler handler) {
        return new ImmersiveBuilderImpl(handler);
    }

    /**
     * Sets the immersive handler for this immersive.
     * @param handler New handler for this immersive.
     * @return Builder object.
     */
    public ImmersiveBuilder setHandler(ImmersiveHandler handler);

    /**
     * Sets the size for items rendered by this immersive.
     * @param size The size of the item when rendering.
     * @return Builder object.
     */
    public ImmersiveBuilder setRenderSize(float size);

    /**
     * Adds a hitbox. Note that item hitboxes MUST be added in slot-order.
     * Aka, the underlying block's slot 0 should be added before its slot 1, etc.
     * @param hitboxInfo HitboxInfo to add. Can use HitboxInfoBuilder to make it easier to create.
     * @return Builder object.
     */
    public ImmersiveBuilder addHitbox(HitboxInfo hitboxInfo);

    /**
     * Adds a 3x3 grid of hitboxes, such as for the crafting table. Adds the top row from left to right,
     * then the middle row from left to right, then the bottom row from left to right.
     * @param hitboxInfo HitboxInfo for center box. Can use HitboxInfoBuilder to make it easier to create.
     * @param distBetweenBoxes Distance between boxes.
     * @return Builder object.
     */
    public ImmersiveBuilder add3x3Grid(HitboxInfo hitboxInfo, double distBetweenBoxes);

    /**
     * Sets the way hitboxes are positioned on the block.
     * @param newMode New mode for positioning.
     * @return Builder object.
     */
    public ImmersiveBuilder setPositioningMode(HitboxPositioningMode newMode);

    /**
     * Sets a function used to retrieve the config value for whether this immerisve is enabled.
     * @param checker Checker to retrieve config value. Something such as () -> ActiveConfig.active().myConfigValue works here.
     * @return Builder object.
     */
    public ImmersiveBuilder setConfigChecker(Supplier<Boolean> checker);

    /**
     * Sets what should happen on right click.
     * @param handler Function that handles a right-click.
     * @return Builder object.
     */
    public ImmersiveBuilder setRightClickHandler(RightClickHandler handler);

    /**
     * Sets the controller num that's used for checking for trigger hitboxes. It's highly recommended to set
     * this to 0 for the primary controller, since that's the controller that can consume the trigger by default.
     * @param controllerNum Controller number.
     * @return Builder object.
     */
    public ImmersiveBuilder setTriggerHitboxControllerNum(int controllerNum);

    /**
     * Sets whether this immersive is only for VR users.
     * @param vrOnly Whether this immersive should now be VR only.
     * @return Builder object.
     */
    public ImmersiveBuilder setVROnly(boolean vrOnly);

    /**
     * Sets a class to be attached to individual info instances to store additional data, such as the anvil's
     * experience levels. Can be set to null (the default) to not create an extra data instance.
     * Note that the supplied class must have a constructor with no parameters.
     * @param clazz Class that represents extra data storage, or null to specify none.
     * @return Builder object.
     */
    public ImmersiveBuilder setExtraInfoDataClass(Class<?> clazz);

    /**
     * Sets a consumer that acts after an incoming NetworkStorage is parsed. For example, this is used
     * for the anvil to retrieve the level amount and store it in extra data.
     * @param storageConsumer New storage consumer.
     * @return Builder object.
     */
    public ImmersiveBuilder setExtraStorageConsumer(BiConsumer<NetworkStorage, BuiltImmersiveInfo> storageConsumer);

    /**
     * Sets a function that determines whether a given slot should be active (rendered, reacts to interactions, etc.).
     * @param slotActive Function that takes an info instance and a slot number and returns whether the slot is active.
     * @return Builder object.
     */
    public ImmersiveBuilder setSlotActiveFunction(BiFunction<BuiltImmersiveInfo, Integer, Boolean> slotActive);

    /**
     * Set function to run on an info before it's removed.
     * @param onRemove Function to run on info just before removal.
     * @return Builder object.
     */
    public ImmersiveBuilder setOnRemove(Consumer<BuiltImmersiveInfo> onRemove);

    /**
     * Set whether to disable right-click interactions on this immersive when the option to disable said
     * interactions is enabled.
     * @param doDisable Whether to disable as described above.
     * @return Builder object.
     */
    public ImmersiveBuilder shouldDisableRightClicksWhenInteractionsDisabled(boolean doDisable);

    /**
     * Set whether the item guide for this slot should be active. This result is &&'d with the built-in checker,
     * which is simply if the slot holds items but currently isn't holding one.
     * @param itemGuideActive Function that returns whether the given slot is active given the info.
     * @return Builder object.
     */
    public ImmersiveBuilder setShouldRenderItemGuideFunction(BiFunction<BuiltImmersiveInfo, Integer, Boolean> itemGuideActive);

    /**
     * Overwrites hitbox at index with a new hitbox. Useful when cloning.
     * @param index Index to overwrite.
     * @param hitboxInfo New hitbox information.
     * @return Builder object.
     */
    public ImmersiveBuilder overwriteHitbox(int index, HitboxInfo hitboxInfo);

    /**
     * Modify a hitbox.
     * @param index Index of hitbox to modify.
     * @param modifier A function that takes the old hitbox as a builder and returns new hitbox info.
     * @return Builder object.
     */
    public ImmersiveBuilder modifyHitbox(int index, Function<HitboxInfoBuilder, HitboxInfo> modifier);

    /**
     * Modify a range of hitboxes, inclusive for both ends.
     * @param startIndex Starting index of range of hitboxes to modify inclusive.
     * @param endIndex Ending index of range of hitboxes to modify inclusive.
     * @param modifier A function that takes the old hitbox as a builder and returns new hitbox info.
     * @return Builder object.
     */
    public ImmersiveBuilder modifyHitboxes(int startIndex, int endIndex, Function<HitboxInfoBuilder, HitboxInfo> modifier);

    /**
     * Create a clone of this ImmersiveBuilder.
     * @return A clone of this ImmersiveBuilder.
     */
    public ImmersiveBuilder clone();
}
