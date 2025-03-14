package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
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

/**
 * Represents the client-side implementation of a block-based Immersive implementation.
 * <br>
 * To clarify what an Immersive is, from the user's perspective, an Immersive is a single category of thing that
 * ImmersiveMC supports. Whether that be furnaces, throwing, or petting, those are all considered Immersives.
 * <br>
 * From a programming perspective, the term Immersive is used to represent a few different concepts:
 * <ol>
 *     <li>The aforementioned user definition of an Immersive.</li>
 *     <li>The aforementioned user definition of an Immersive, but only for implementations that take advantage
 *     of this interface. This is also referred to as a block-based Immersive.</li>
 *     <li>The client-side exclusive logic of a block-based Immersive.</li>
 * </ol>
 * The final entry on that list is what this interface is; "The client-side exclusive logic of a block-based Immersive."
 * Developers implement this interface to create the client-side implementation of a block-based Immersive (or,
 * alternatively, build one using an {@link ImmersiveBuilder}). When combined with a {@link ImmersiveHandler}, you have
 * a fully-functioning (block-based) Immersive!
 * @param <I> The {@link ImmersiveInfo} implementation this Immersive uses.
 * @param <S> The type of storage to use for sending Immersive data over the network.
 */
public interface Immersive<I extends ImmersiveInfo, S extends NetworkStorage> {

    /**
     * Get the collection of ImmersiveInfos currently active for this Immersive. The contents of the list may be
     * modified by ImmersiveMC with the intention of updating the actual set of active ImmersiveInfos.
     * <br>
     * For example, if this Immersive represented a furnace, and the furnace was broken, ImmersiveMC would remove the
     * ImmersiveInfo from the collection returned by this function to indicate that this Immersive should no longer
     * handle the block, as it is no longer a furnace. As another example, if this Immersive represented a furnace, and
     * a player placed a furnace, ImmersiveMC would add the result of {@link #buildInfo(BlockPos, Level)} to the collection
     * returned by this function.
     * <br>
     * In short, you should the actual collection of ImmersiveInfos used by this Immersive instead of a copy of it,
     * unless you want to deal with a lot of extra work.
     * @return The collection of all ImmersiveInfos tied to this Immersive.
     */
    public Collection<I> getTrackedObjects();

    /**
     * Constructs a new ImmersiveInfo based on the provided block position. It's best to calculate initial hitboxes,
     * etc. in this method to make the Immersive available for interaction as soon as possible.
     *
     * @param pos The position of a block that matches this Immersive.
     * @param level The level in which this info is being built.
     * @return An instance of an ImmersiveInfo implementation with the same position as provided.
     */
    public I buildInfo(BlockPos pos, Level level);

    /**
     * The method called when a player interacts with a hitbox.
     * <br>
     * If multiple hitboxes are being interacted with at the same time, only the first hitbox in iteration order from
     * {@link ImmersiveInfo#getAllHitboxes()} that is being interacted with will have this function called.
     *
     * @param info The info containing the hitbox that was interacted with.
     * @param player The player that interacted with the hitbox. This player is always the player currently controlling
     *               the game window.
     * @param hitboxIndices The indices into {@link ImmersiveInfo#getAllHitboxes()} that were interacted with. The list
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
    public int handleHitboxInteract(I info, LocalPlayer player, List<Integer> hitboxIndices, InteractionHand hand, boolean modifierPressed);

    /**
     * This method is called once per game tick. This is where you should, for example, recalculate hitboxes if needed.
     * @param info The info being ticked.
     */
    public void tick(I info);

    /**
     * @return The hitbox that determines whether dragging between multiple slots should continue or not. Can return
     * null here to not allow dragging. If non-null, the hitbox should contain all hitboxes where
     * {@link #isInputHitbox(ImmersiveInfo, int)} returns true.
     */
    @Nullable
    public BoundingBox getDragHitbox(I info);

    /**
     * @param info The info being checked with.
     * @param hitboxIndex The hitbox index being checked.
     * @return Whether the provided hitbox index is an input, such as for inputting items.
     */
    public boolean isInputHitbox(I info, int hitboxIndex);

    /**
     * Whether the provided info should render in the world. It's good to return false here if this Immersive
     * does not have its data ready for rendering.
     * @param info The info to check.
     * @return Whether the provided info should render to the world, which includes calling
     *         {@link #render(ImmersiveInfo, PoseStack, ImmersiveRenderHelpers, float)}.
     */
    public boolean shouldRender(I info);

    /**
     * Render the provided info.
     *
     * @param info The info to render.
     * @param stack The pose stack being rendered with.
     * @param helpers Some helper functions for rendering.
     * @param partialTick The fraction of time between the last tick and the current tick.
     */
    public void render(I info, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTick);

    /**
     * @return The {@link ImmersiveHandler} this Immersive uses.
     */
    public ImmersiveHandler<S> getHandler();

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
    @Nullable
    public ImmersiveConfigScreenInfo configScreenInfo();

    /**
     * Whether normal right-click behavior for this block should be disabled when the option to disable interactions is
     * enabled in ImmersiveMC. This should usually return true if the block opens a GUI on right-click.
     * @param info The info for the block that may want to disable right-clicks.
     * @return Whether to skip right-click behavior for this block when the option to disable click interactions is
     *         enabled in ImmersiveMC.
     */
    public boolean shouldDisableRightClicksWhenVanillaInteractionsDisabled(I info);

    /**
     * Process the storage from the server for this Immersive. Not called for Immersives that return
     * true for {@link #getHandler()}'s {@link ImmersiveHandler#clientAuthoritative()}.
     * @param info The info with storage being processed.
     * @param storage The storage to be processed.
     */
    public void processStorageFromNetwork(I info, S storage);

    /**
     * @return Whether this Immersive should only exist for VR users. The same value should always be returned by this
     *         method.
     */
    public boolean isVROnly();

    /**
     * This is the same as {@link #tick(ImmersiveInfo)}, but called once per tick, instead of called once per tick
     * per info.
     */
    default void globalTick() {}
}
