package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.client.immersive.ImmersiveBuilderImpl;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.function.*;

/**
 * An alternative to {@link Immersive} to make (block-based) Immersives with much less effort, while still getting
 * a significant amount of flexibility. Note that the underlying defaults if a given builder method isn't called are
 * not part of the API, and may change in the future.
 * @param <E> The type of "extra data" to add to {@link BuiltImmersiveInfo} instances. This way, data other than what
 *           ImmersiveMC keeps track of can be used. This type MUST have a public constructor that takes 0 arguments.
 * @param <S> The type of storage to use for sending Immersive data over the network.
 */
public interface ImmersiveBuilder<E, S extends NetworkStorage> {

    /**
     * Create an ImmersiveBuilder to start making an Immersive. You cannot use ImmersiveBuilders with
     * MultiblockImmersiveHandlers; write an {@link Immersive} implementation instead to use those.
     * @param handler The handler for the Immersive.
     * @return A builder object.
     * @throws IllegalArgumentException If provided a MultiblockImmersiveHandler, as it is unsupported with
     * ImmersiveBuilders.
     */
    public static <NS extends NetworkStorage> ImmersiveBuilder<?, NS> create(ImmersiveHandler<NS> handler) throws IllegalArgumentException {
        return new ImmersiveBuilderImpl<>(handler, null);
    }

    /**
     * Create an ImmersiveBuilder to start making an Immersive. You cannot use ImmersiveBuilders with
     * MultiblockImmersiveHandlers; write an {@link Immersive} implementation instead to use those.
     * @param handler The handler for the Immersive.
     * @param extraInfoDataClass A class with an empty constructor that holds extra data for each info instance.
     * @return A builder object.
     * @throws IllegalArgumentException If provided a MultiblockImmersiveHandler, as it is unsupported with
     * ImmersiveBuilders.
     */
    public static <E, NS extends NetworkStorage> ImmersiveBuilder<E, NS> create(ImmersiveHandler<NS> handler, Class<E> extraInfoDataClass) throws IllegalArgumentException {
        return new ImmersiveBuilderImpl<>(handler, extraInfoDataClass);
    }

    /**
     * Sets the size for items rendered by this immersive.
     * @param size The size of the item when rendering.
     * @return Builder object.
     */
    public ImmersiveBuilder<E,S> setRenderSize(float size);

    /**
     * Adds a hitbox. Note that item hitboxes MUST be added in slot-order.
     * Aka, the underlying block's slot 0 should be added before its slot 1, etc.
     * @param relativeHitboxInfo HitboxInfo to add. Can use HitboxInfoBuilder to make it easier to create.
     * @return Builder object.
     */
    public ImmersiveBuilder<E,S> addHitbox(RelativeHitboxInfo relativeHitboxInfo);

    /**
     * Adds a 3x3 grid of hitboxes, such as for the crafting table. Adds the top row from left to right,
     * then the middle row from left to right, then the bottom row from left to right.
     * @param relativeHitboxInfo HitboxInfo for center box. Can use HitboxInfoBuilder to make it easier to create.
     * @param distBetweenBoxes Distance between boxes.
     * @return Builder object.
     */
    public ImmersiveBuilder<E,S> add3x3Grid(RelativeHitboxInfo relativeHitboxInfo, double distBetweenBoxes);

    /**
     * Sets the way hitboxes are positioned on the block.
     * @param newMode New mode for positioning.
     * @return Builder object.
     */
    public ImmersiveBuilder<E,S> setPositioningMode(HitboxPositioningMode newMode);

    /**
     * Sets what should happen when a hitbox is interacted with.
     * @param handler Function that takes an info instance, a player doing the interaction, the slot being interacted
     *                with, and the hand being interacted with. This function should return a number denoting the
     *                cooldown until the user can interact with Immersives again, or a negative number to denote that
     *                no interaction took place. The returned cooldown is increased for VR users, unless the Immersive
     *                is VR-only.
     * @return Builder object.
     */
    public ImmersiveBuilder<E,S> setHitboxInteractHandler(HitboxInteractHandler<E> handler);

    /**
     * Sets whether this immersive is only for VR users.
     * @param vrOnly Whether this immersive should now be VR only.
     * @return Builder object.
     */
    public ImmersiveBuilder<E,S> setVROnly(boolean vrOnly);

    /**
     * Sets a consumer that acts after an incoming NetworkStorage is parsed. For example, this is used
     * for the anvil to retrieve the level amount and store it in extra data.
     * @param storageConsumer New storage consumer.
     * @return Builder object.
     */
    public ImmersiveBuilder<E,S> setExtraStorageConsumer(BiConsumer<S, BuiltImmersiveInfo<E>> storageConsumer);

    /**
     * Sets a function that determines whether a given slot should be active (rendered, reacts to interactions, etc.).
     * @param slotActive Function that takes an info instance and a slot number and returns whether the slot is active.
     * @return Builder object.
     */
    public ImmersiveBuilder<E,S> setSlotActiveFunction(BiFunction<BuiltImmersiveInfo<E>, Integer, Boolean> slotActive);

    /**
     * Set function to run on an info before it's removed.
     * @param onRemove Function to run on info just before removal.
     * @return Builder object.
     */
    public ImmersiveBuilder<E,S> setOnRemove(Consumer<BuiltImmersiveInfo<E>> onRemove);

    /**
     * Set whether to disable right-click interactions on this immersive when the option to disable said
     * interactions is enabled.
     * @param doDisable Whether to disable as described above.
     * @return Builder object.
     */
    public ImmersiveBuilder<E,S> shouldDisableRightClicksWhenInteractionsDisabled(boolean doDisable);

    /**
     * Set whether the item guide for this slot should be active. This result is AND'd with the built-in checker,
     * which is simply if the slot holds items but currently isn't holding one.
     * @param itemGuideActive Function that returns whether the given slot is active given the info.
     * @return Builder object.
     */
    public ImmersiveBuilder<E,S> setShouldRenderItemGuideFunction(BiFunction<BuiltImmersiveInfo<E>, Integer, Boolean> itemGuideActive);

    /**
     * Set the config screen info associated with this Immersive.
     * @param info The config screen info to associate with this Immersive.
     * @return Builder object.
     */
    public ImmersiveBuilder<E,S> setConfigScreenInfo(ImmersiveConfigScreenInfo info);

    /**
     * Set an extra function to run when rendering.
     * @param renderer Extra function to run when rendering, taking the info and the light for the Immersive.
     * @return Builder object.
     */
    public ImmersiveBuilder<E,S> setExtraRenderer(ExtraRenderer<E> renderer);

    /**
     * Overwrites hitbox at index with a new hitbox. Useful when cloning.
     * @param index Index to overwrite.
     * @param relativeHitboxInfo New hitbox information.
     * @return Builder object.
     */
    public ImmersiveBuilder<E,S> overwriteHitbox(int index, RelativeHitboxInfo relativeHitboxInfo);

    /**
     * Modify a hitbox.
     * @param index Index of hitbox to modify.
     * @param modifier A function that takes the old hitbox as a builder and returns new hitbox info.
     * @return Builder object.
     */
    public ImmersiveBuilder<E,S> modifyHitbox(int index, Function<RelativeHitboxInfoBuilder, RelativeHitboxInfo> modifier);

    /**
     * Modify a range of hitboxes, inclusive for both ends.
     * @param startIndex Starting index of range of hitboxes to modify inclusive.
     * @param endIndex Ending index of range of hitboxes to modify inclusive.
     * @param modifier A function that takes the old hitbox as a builder and returns new hitbox info.
     * @return Builder object.
     */
    public ImmersiveBuilder<E,S> modifyHitboxes(int startIndex, int endIndex, Function<RelativeHitboxInfoBuilder, RelativeHitboxInfo> modifier);

    /**
     * Sets the function used to generate the drag hitbox. See {@link Immersive#getDragHitbox(ImmersiveInfo)} for info
     * about what the drag hitbox is.
     * @param dragHitboxCreator The drag hitbox creator or null (the default) to let ImmersiveMC generate one for you.
     * @return Builder object.
     */
    public ImmersiveBuilder<E,S> setDragHitboxCreator(@Nullable Function<BuiltImmersiveInfo<E>, AABB> dragHitboxCreator);

    /**
     * Sets the drag hitbox creator to a creator such that this Immersive never has a drag hitbox. This is equivalent
     * to setting the drag hitbox creator to a function that always returns null, such as {@code info -> null}.
     * @return Builder object.
     */
    default ImmersiveBuilder<E,S> setNoDragHitbox() {
        return setDragHitboxCreator(info -> null);
    }

    /**
     * Create a copy of this ImmersiveBuilder, setting the extra storage consumer and the Immersive config info to null
     * on the copy.
     * @return A best-effort copy of this ImmersiveBuilder.
     */
    public <T extends NetworkStorage> ImmersiveBuilder<E, T> copy(ImmersiveHandler<T> newHandler);

    /**
     * Create a copy of this ImmersiveBuilder, setting the extra storage consumer, the extra render ready,
     * the slot active function, the on remove function, the slot renders item guide function, the right click
     * handler, and the Immersive config info to null/no-op on the copy.
     * @return A best-effort copy of this ImmersiveBuilder.
     */
    public <F, T extends NetworkStorage> ImmersiveBuilder<F, T> copy(ImmersiveHandler<T> newHandler, Class<F> newExtraInfoDataClass);

    /**
     * Builds this Immersive.
     * @return The built Immersive from this builder.
     */
    public BuiltImmersive<E, S> build();
}
