package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import com.hammy275.immersivemc.client.immersive.info.DragImmersiveInfo;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRBodyPartData;

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
     * <ul>
     *     <li>All integers in nonInteractable represent indexes into {@link DragImmersiveInfo#hitboxes} which
     *     <i>will</i> count for dragging but will <i>never</i> be passed to
     *     {@link #hitboxDragged(DragImmersiveInfo, int, int, int)} as either the old or new index. This is mainly
     *     intended for when you want to allow dragging between some hitboxes, but need an outer hitbox that keeps the
     *     dragging going; this function should return the index for that outer hitbox.</li>
     *     <li>makeHitboxesEveryTick determines whether {@link #makeHitboxes(DragImmersiveInfo, Level)} is called
     *     every tick.</li>
     * </ul>
     * @return Whether to use auto-dragging.
     */
    protected abstract AutoDragSettings autoDragSettings();

    /**
     * Add/set hitboxes into the info instance. Called automatically on each tick if
     * {@link AutoDragSettings#makeHitboxesEveryTick} is true. Otherwise, it's not called.
     * @param info Info to set hitboxes into.
     * @param level The current level.
     */
    protected void makeHitboxes(DragImmersiveInfo info, Level level) {}

    @Override
    public Collection<DragImmersiveInfo> getTrackedObjects() {
        return trackedObjects;
    }

    @Override
    public int handleHitboxInteract(DragImmersiveInfo info, LocalPlayer player, List<Integer> hitboxIndices, InteractionHand hand, boolean modifierPressed) {
        return -1; // Cooldown isn't handled by Immersive system since we interact with hitboxes nonstop
    }

    @Override
    public boolean shouldRender(DragImmersiveInfo info) {
        return true;
    }

    @Override
    public void render(DragImmersiveInfo info, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTick) {
        for (int i = 0; i < info.hitboxes.size(); i++) {
            HitboxInfo hitbox = info.hitboxes.get(i);
            AutoDragSettings autoDrag = autoDragSettings();
            float blue = info.startingHitboxIndex != i ? 1 : 0;
            float red = !autoDrag.nonInteractables.contains(i) ? blue : 0;
            helpers.renderHitbox(stack, hitbox.getHitbox(), false, red, 1, blue);
        }
    }

    @Override
    public void tick(DragImmersiveInfo info) {
        AutoDragSettings autoDrag = autoDragSettings();
        info.ticksExisted++;
        if (autoDrag.makeHitboxesEveryTick) {
            makeHitboxes(info, Minecraft.getInstance().level);
        }
        for (InteractionHand interactionHand : InteractionHand.values()) {
            VRBodyPartData hand = VRClientAPI.instance().getPreTickWorldPose().getHand(interactionHand);
            int lastGrabbed = info.grabbedBox[interactionHand.ordinal()];
            int grabbed = Util.getFirstIntersect(hand.getPos(), info.getAllHitboxes().stream().map(HitboxInfo::getHitbox).toList()).orElse(-1);
            if ((lastGrabbed == info.startingHitboxIndex || info.startingHitboxIndex == -1) && grabbed > -1 && lastGrabbed != grabbed) {
                if (!autoDrag.nonInteractables.contains(grabbed)) {
                    hitboxDragged(info, interactionHand.ordinal(), lastGrabbed, grabbed);
                }
            }
            if (!autoDrag.nonInteractables.contains(grabbed)) {
                info.grabbedBox[interactionHand.ordinal()] = grabbed;
            }
        }
    }

    @Override
    @Nullable
    public AABB getDragHitbox(DragImmersiveInfo info) {
        return null;
    }

    @Override
    public boolean isInputHitbox(DragImmersiveInfo info, int hitboxIndex) {
        return true;
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

    public record AutoDragSettings(Collection<Integer> nonInteractables,
                                   boolean makeHitboxesEveryTick) {}
}
