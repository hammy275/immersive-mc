package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.immersive.BlockBasedImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public sealed interface Immersive<I extends ImmersiveInfo, R extends ImmersiveRenderState, S extends NetworkStorage>
        permits BlockBasedImmersive {
    /**
     * Get the collection of ImmersiveInfos currently active for this Immersive. The contents of the list may be
     * modified by ImmersiveMC with the intention of updating the actual set of active ImmersiveInfos.
     * <br>
     * For example, if this Immersive represented a furnace, and the furnace was broken, ImmersiveMC would remove the
     * ImmersiveInfo from the collection returned by this function to indicate that this Immersive should no longer
     * handle the block, as it is no longer a furnace. As another example, if this Immersive represented a furnace, and
     * a player placed a furnace, ImmersiveMC would add the result of
     * {@link BlockBasedImmersive#buildInfo(BlockPos, Level)} to the collection returned by this function.
     * <br>
     * In short, you should return the actual collection of ImmersiveInfos used by this Immersive instead of a copy of
     * it.
     * @return The collection of all ImmersiveInfos tied to this Immersive.
     */
    Collection<I> getTrackedObjects();

    /**
     * The method called when a player interacts with a hitbox.
     * <br>
     * If multiple hitboxes are being interacted with at the same time, only the first hitbox in iteration order from
     * {@link BlockBasedImmersiveInfo#getAllHitboxes()} that is being interacted with will have this function called.
     *
     * @param info The info containing the hitbox that was interacted with.
     * @param player The player that interacted with the hitbox. This player is always the player currently controlling
     *               the game window.
     * @param hitboxIndices The indices into {@link BlockBasedImmersiveInfo#getAllHitboxes()} that were interacted with. The list
     *                      is guaranteed to contain at least one element and all elements are not null.
     * @param hand The hand used for interaction.
     * @param modifierPressed Whether the modifier key (usually the button mapped to breaking blocks) was held for the
     *                        interaction.
     * @return A number representing the number of ticks of cooldown to apply before the player can interact with
     *         any Immersive again, or a negative number to denote no actual interaction has happened, such as
     *         obtaining items from an output slot of an Immersive when the output slot has no items. This cooldown
     *         should be the cooldown for desktop users if {@link #isVROnly()} returns false, and it should be the
     *         cooldown for VR users if {@link #isVROnly()} returns true. ImmersiveMC will modify this cooldown time
     *         to accommodate situations, such as VR users requiring an increased cooldown time.
     */
    int handleHitboxInteract(I info, LocalPlayer player, List<Integer> hitboxIndices, InteractionHand hand, boolean modifierPressed);

    /**
     * This method is called once per game tick. This is where you should, for example, recalculate hitboxes if needed.
     * @param info The info being ticked.
     */
    void tick(I info);

    /**
     * @return The hitbox that determines whether dragging between multiple slots should continue or not. Can return
     * null here to not allow dragging. If non-null, the hitbox should contain all hitboxes where
     * {@link #isInputHitbox(ImmersiveInfo, int)} returns true.
     */
    @Nullable BoundingBox getDragHitbox(I info);

    /**
     * @param info The info being checked with.
     * @param hitboxIndex The hitbox index being checked.
     * @return Whether the provided hitbox index is an input, such as for inputting items.
     */
    boolean isInputHitbox(I info, int hitboxIndex);

    /**
     * Whether the provided render state should render in the world. It's good to return false here if this Immersive
     * does not have its data ready for rendering.
     * @param renderState The render state to check.
     * @return Whether the provided render state should render to the world, which includes calling
     * {@link #render(ImmersiveRenderState, PoseStack, ImmersiveRenderHelpers, float)}.
     */
    boolean shouldRender(R renderState);

    /**
     * Render the provided render state.
     *
     * @param renderState The render state to render.
     * @param stack The pose stack being rendered with.
     * @param helpers Some helper functions for rendering.
     * @param partialTick The fraction of time between the last tick and the current tick.
     */
    void render(R renderState, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTick);

    /**
     * @return The {@link ImmersiveHandler} this Immersive uses.
     */
    ImmersiveHandler getHandler();

    /**
     * The info needed to build a config screen button for this Immersive. If this method returns null, ImmersiveMC
     * will not add a setting for this Immersive to its in-game configuration. Reasons to possibly return null from this
     * method include, but are not limited to:
     * <ul>
     *     <li>This Immersive cannot be controlled via a config.</li>
     *     <li>Another mod already handles configuring this Immersive.</li>
     * </ul>
     * @return An ImmersiveConfigScreenInfo instance used for ImmersiveMC to add this Immersive to its in-game
     *         configuration screens, or null if ImmersiveMC should not do so.
     */
    @Nullable ImmersiveConfigScreenInfo configScreenInfo();

    /**
     * Process the storage from the server for this Immersive. Not called for Immersives that return
     * true for {@link #getHandler()}'s {@link BlockBasedImmersiveHandler#clientAuthoritative()}.
     * @param info The info with storage being processed.
     * @param storage The storage to be processed.
     */
    void processStorageFromNetwork(I info, S storage);

    /**
     * @return Whether this Immersive should only exist for VR users. The same value should always be returned by this
     *         method.
     */
    boolean isVROnly();

    /**
     * Creates a new render state not populated with any rendering-related details.
     * @return Created render state, as described.
     */
    R createRenderState();

    /**
     * Extract render state from the info into the provided render state object.
     * <p>
     * The extracted render state must be independent of the info it comes from such that mutations to the info
     * do not affect the render state. For example, if the info contains a list of hitboxes, a modification to that
     * list must NOT modify the list of hitboxes stored in the render state.
     *
     * @param info Info to extract render state from.
     * @param renderState Render state to populate from the info. Note that the API does NOT cover the state of this
     *                    object when this function is called. It only defines that this should be fully populated from
     *                    the provided info for rendering. As such, it's recommended to set the values for all fields
     *                    in this object based on the provided info every time this method is called.
     * @param partialTicks The fraction of time between the last tick and the current tick.
     */
    void extractRenderState(I info, R renderState, float partialTicks);

    /**
     * This is the same as {@link #tick(ImmersiveInfo)}, but called once per tick, instead of called once per tick per
     * info.
     */
    default void globalTick() {}
}
