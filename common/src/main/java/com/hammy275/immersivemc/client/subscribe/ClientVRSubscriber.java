package com.hammy275.immersivemc.client.subscribe;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.api.client.immersive.BlockBasedImmersive;
import com.hammy275.immersivemc.api.client.immersive.BlockBasedImmersiveInfo;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.client.immersive.AbstractPlayerAttachmentImmersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.SwapTracker;
import com.hammy275.immersivemc.client.immersive.info.AbstractPlayerAttachmentInfo;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VR;
import com.hammy275.immersivemc.common.vr.VRVerify;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;

import java.util.List;
import java.util.Optional;

public class ClientVRSubscriber {

    // Global cooldown to prevent rapid-fire VR interactions
    protected static int cooldown = 0;

    public static void setCooldown(int cooldown) {
        cooldown = cooldown == 0 ? 1 : cooldown; // A cooldown of 0 and 1 are functionally the same
        ClientVRSubscriber.cooldown = Math.max(ClientVRSubscriber.cooldown, cooldown);
    }

    public static int getCooldown() {
        return cooldown;
    }

    public static void immersiveTickVR(Player player) {
        if (!Platform.COMMON.isClient()) return;
        if (Minecraft.getInstance().gameMode == null) return;
        if (!VRVerify.playerInVR(player)) return;
        VRPose vrPose = VR.API.getVRPose(player);

        // Track things the HMD is looking at (cursor is already covered in ClientLogicSubscriber)
        double dist = Minecraft.getInstance().player.blockInteractionRange();
        Vec3 start = vrPose.getHead().getPos();
        Vec3 look = vrPose.getHead().getDir();
        Vec3 end = vrPose.getHead().getPos().add(look.x * dist, look.y * dist, look.z * dist);
        BlockHitResult res = player.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE,
                player));
        ClientLogicSubscriber.possiblyTrack(res.getBlockPos(), player.level().getBlockState(res.getBlockPos()),
                player.level().getBlockEntity(res.getBlockPos()), Minecraft.getInstance().level);

        if (cooldown > 0) {
            cooldown--;
        }

        for (InteractionHand hand : InteractionHand.values()) {
            for (Immersive<?, ?, ?> singleton : Immersives.ALL_IMMERSIVES) {
                if (handleInfos(singleton, vrPose, hand)) {
                    return;
                }
            }
            SwapTracker swapTracker = hand == InteractionHand.MAIN_HAND ? SwapTracker.c0 : SwapTracker.c1;
            swapTracker.tick(null, null, -1, false);
        }

        if (cooldown <= 0) {
            for (AbstractPlayerAttachmentImmersive<? extends AbstractPlayerAttachmentInfo, ?> singleton : Immersives.IMMERSIVE_ATTACHMENTS) {
                for (AbstractPlayerAttachmentInfo info : singleton.getTrackedObjects()) {
                    if (handleInfo(singleton, info, vrPose)) {
                        return;
                    }
                }
            }
        }
    }

    protected static <I extends ImmersiveInfo> boolean handleInfos(Immersive<I, ?, ?> singleton, VRPose vrPose, InteractionHand hand) {
        I infoWithDragHitbox = null;
        SwapTracker swapTracker = hand == InteractionHand.MAIN_HAND ? SwapTracker.c0 : SwapTracker.c1;
        for (I info : singleton.getTrackedObjects()) {
            if (info.hasHitboxes()) {
                VRBodyPartData controller = vrPose.getHand(hand);
                Vec3 pos = controller.getPos();
                Optional<Integer> hit = Util.getFirstIntersect(pos, info.getAllHitboxes());
                if (hit.isPresent() &&
                        (Minecraft.getInstance().options.keyAttack.isDown() || !info.getAllHitboxes().get(hit.get()).isTriggerHitbox())) {
                    if (singleton.isInputHitbox(info, hit.get())) {
                        swapTracker.tick(singleton, info, hit.get(), true);
                        return swapTracker.getCooldown() >= 0;
                    } else {
                        swapTracker.tick(singleton, info, -1, inDragHitbox(singleton, info, pos));
                        if (cooldown <= 0) {
                            int cooldown = singleton.handleHitboxInteract(info, Minecraft.getInstance().player, List.of(hit.get()), hand, Minecraft.getInstance().options.keyAttack.isDown());
                            if (singleton.isVROnly()) {
                                cooldown = (int) (cooldown / 1.5);
                            }
                            setCooldown(cooldown);
                        }
                        return cooldown >= 0;
                    }
                } else if (hit.isEmpty()) {
                    if (inDragHitbox(singleton, info, pos)) {
                        infoWithDragHitbox = info;
                    }
                }
            }
        }
        if (infoWithDragHitbox != null) {
            swapTracker.tick(singleton, infoWithDragHitbox, -1, true);
        }
        return infoWithDragHitbox != null;
    }

    protected static boolean handleInfo(AbstractPlayerAttachmentImmersive<?, ?> singleton, AbstractPlayerAttachmentInfo info, VRPose vrPose) {
        if (info.hasHitboxes() && singleton.hitboxesAvailable(info)) {
            for (InteractionHand hand : InteractionHand.values()) {
                VRBodyPartData controller = vrPose.getHand(hand);
                Vec3 pos = controller.getPos();
                Optional<Integer> hit = Util.getFirstIntersect(pos, info.getAllHitboxes());
                if (hit.isPresent()) {
                    singleton.onAnyRightClick(info);
                    singleton.handleRightClick(info, Minecraft.getInstance().player, hit.get(), hand);
                    if (Minecraft.getInstance().options.keyAttack.isDown()) {
                        cooldown = 20; // Set long cooldown if whole stack is placed
                    } else {
                        cooldown = singleton.getCooldownVR();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static <I extends ImmersiveInfo> boolean inDragHitbox(Immersive<I, ?, ?> singleton, I info, Vec3 pos) {
        BoundingBox dragHitbox = singleton.getDragHitbox(info);
        return dragHitbox != null && Util.getFirstIntersect(pos, dragHitbox).isPresent();
    }
}
