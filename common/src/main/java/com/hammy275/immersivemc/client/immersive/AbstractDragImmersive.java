package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.immersive.info.DragImmersiveInfo;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.mojang.blaze3d.vertex.PoseStack;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Immersive that's used for dragging between hitboxes, such as trapdoors.
 */
public abstract class AbstractDragImmersive implements Immersive<DragImmersiveInfo, NullStorage> {

    protected final List<DragImmersiveInfo> trackedObjects = new ArrayList<>();

    /**
     * Called whenever a drag occurs from some old hitbox index to a new one. This is an ideal spot to update
     * {@link DragImmersiveInfo#startingHitboxIndex} if not auto-dragging.
     *
     * @param info Info with dragging happening.
     * @param controller Controller number that is dragging.
     * @param oldIndex Old hitbox index. This will be -1 if starting a drag from "anywhere".
     * @param newIndex New hitbox index. This will never be -1.
     */
    protected abstract void hitboxDragged(DragImmersiveInfo info, int controller, int oldIndex, int newIndex);

    /**
     * Configures auto-dragging.
     * <br>
     * When this returns null, auto-dragging does not occur; no special behavior.
     * <br>
     * When this returns non-null, auto-dragging will occur. Auto-dragging does the following:
     * <ul>
     *     <li>If updateStartingIndex is true, {@link DragImmersiveInfo#startingHitboxIndex} is updated to be whatever
     *     the last dragged to hitbox is, meaning it does not need to be set manually in
     *     {@link #hitboxDragged(DragImmersiveInfo, int, int, int)} or similar.</li>
     *     <li>All integers in nonInteractable represent indexes into {@link DragImmersiveInfo#hitboxes} which
     *     <i>will</i> count for dragging but will <i>never</i> be passed to
     *     {@link #hitboxDragged(DragImmersiveInfo, int, int, int)} as either the old or new index. This is mainly
     *     intended for when you want to allow dragging between some hitboxes, but need an outer hitbox that keeps the
     *     dragging going; this function should return the index for that outer hitbox.</li>
     * </ul>
     *  The
     * integers
     * @return Whether to use auto-dragging.
     */
    @Nullable
    protected abstract AutoDragSettings autoDraggingData();

    /**
     * Add/set hitboxes into the info instance. Called automatically on each tick, since the underlying block may
     * change state non-Immersively.
     * @param info Info to set hitboxes into.
     * @param level The current level.
     */
    protected abstract void makeHitboxes(DragImmersiveInfo info, Level level);

    @Override
    public Collection<DragImmersiveInfo> getTrackedObjects() {
        return trackedObjects;
    }

    @Override
    public int handleHitboxInteract(DragImmersiveInfo info, LocalPlayer player, int hitboxIndex, InteractionHand hand) {
        return -1; // Cooldown isn't handled by Immersive system since we interact with hitboxes nonstop
    }

    @Override
    public boolean shouldRender(DragImmersiveInfo info) {
        return true;
    }

    @Override
    public void render(DragImmersiveInfo info, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTicks) {
        for (int i = 0; i < info.hitboxes.size(); i++) {
            HitboxInfo hitbox = info.hitboxes.get(i);
            AutoDragSettings autoDrag = autoDraggingData();
            float red = autoDrag == null || !autoDrag.nonInteractables.contains(i) ? 1 : 0;
            helpers.renderHitbox(stack, hitbox.getHitbox(), false, red, 1, 1);
        }
    }

    @Override
    public void tick(DragImmersiveInfo info) {
        info.ticksExisted++;
        makeHitboxes(info, Minecraft.getInstance().level);
        AutoDragSettings autoDrag = autoDraggingData();
        for (int c = 0; c <= 1; c++) {
            IVRData hand = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController(c);
            int lastGrabbed = info.grabbedBox[c];
            int grabbed = Util.getFirstIntersect(hand.position(), info.getAllHitboxes().stream().map(HitboxInfo::getHitbox).toList()).orElse(-1);
            if ((lastGrabbed == info.startingHitboxIndex || info.startingHitboxIndex == -1) && grabbed > -1 && lastGrabbed != grabbed) {
                if (autoDrag == null || !autoDrag.nonInteractables.contains(grabbed)) {
                    hitboxDragged(info, c, lastGrabbed, grabbed);
                    if (autoDrag != null && autoDrag.updateStartingIndex) {
                        info.startingHitboxIndex = grabbed;
                    }
                }
            }
            if (autoDrag == null || !autoDrag.nonInteractables.contains(grabbed)) {
                info.grabbedBox[c] = grabbed;
            }
        }
    }

    @Override
    public ImmersiveHandler<NullStorage> getHandler() {
        return ImmersiveHandlers.trapdoorHandler;
    }

    @Override
    public boolean shouldDisableRightClicksWhenVanillaInteractionsDisabled(DragImmersiveInfo info) {
        return true;
    }

    @Override
    public void processStorageFromNetwork(DragImmersiveInfo info, NullStorage storage) {
        // No storage to process, so intentional no-op
    }

    @Override
    public boolean isVROnly() {
        return true;
    }

    public record AutoDragSettings(Collection<Integer> nonInteractables, boolean updateStartingIndex) {}
}
