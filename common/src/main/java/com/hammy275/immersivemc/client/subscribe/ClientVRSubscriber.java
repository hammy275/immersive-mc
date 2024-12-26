package com.hammy275.immersivemc.client.subscribe;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.client.immersive.AbstractPlayerAttachmentImmersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.SwapTracker;
import com.hammy275.immersivemc.client.immersive.info.AbstractPlayerAttachmentInfo;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class ClientVRSubscriber {

    // Global cooldown to prevent rapid-fire VR interactions
    protected static int cooldown = 0;

    public static void setCooldown(int cooldown) {
        ClientVRSubscriber.cooldown = Math.max(ClientVRSubscriber.cooldown, cooldown);
    }

    public static int getCooldown() {
        return cooldown;
    }

    public static void immersiveTickVR(Player player) {
        if (!Platform.isClient()) return;
        if (Minecraft.getInstance().gameMode == null) return;
        if (!VRPlugin.API.playerInVR(player)) return;
        IVRPlayer vrPlayer = VRPlugin.API.getVRPlayer(player);

        // Track things the HMD is looking at (cursor is already covered in ClientLogicSubscriber)
        double dist = Minecraft.getInstance().player.blockInteractionRange();
        Vec3 start = vrPlayer.getHMD().position();
        Vec3 look = vrPlayer.getHMD().getLookAngle();
        Vec3 end = vrPlayer.getHMD().position().add(look.x * dist, look.y * dist, look.z * dist);
        BlockHitResult res = player.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE,
                player));
        ClientLogicSubscriber.possiblyTrack(res.getBlockPos(), player.level().getBlockState(res.getBlockPos()),
                player.level().getBlockEntity(res.getBlockPos()), Minecraft.getInstance().level);

        if (cooldown > 0) {
            cooldown--;
        }

        for (int c = 0; c <= 1; c++) {
            for (Immersive<?, ?> singleton : Immersives.IMMERSIVES) {
                if (handleInfos(singleton, vrPlayer, c)) {
                    return;
                }
            }
            SwapTracker swapTracker = c == 0 ? SwapTracker.c0 : SwapTracker.c1;
            swapTracker.tick(null, null, -1, false);
        }

        if (cooldown <= 0) {
            for (AbstractPlayerAttachmentImmersive<? extends AbstractPlayerAttachmentInfo, ?> singleton : Immersives.IMMERSIVE_ATTACHMENTS) {
                for (AbstractPlayerAttachmentInfo info : singleton.getTrackedObjects()) {
                    if (handleInfo(singleton, info, vrPlayer)) {
                        return;
                    }
                }
            }
        }
    }

    protected static <I extends ImmersiveInfo> boolean handleInfos(Immersive<I, ?> singleton, IVRPlayer vrPlayer, int c) {
        I infoWithDragHitbox = null;
        SwapTracker swapTracker = c == 0 ? SwapTracker.c0 : SwapTracker.c1;
        for (I info : singleton.getTrackedObjects()) {
            if (info.hasHitboxes()) {
                IVRData controller = vrPlayer.getController(c);
                Vec3 pos = controller.position();
                Optional<Integer> hit = Util.getFirstIntersect(pos, info.getAllHitboxes());
                if (hit.isPresent() &&
                        (Minecraft.getInstance().options.keyAttack.isDown() || !info.getAllHitboxes().get(hit.get()).isTriggerHitbox())) {
                    if (singleton.isInputHitbox(info, hit.get())) {
                        swapTracker.tick(singleton, info, hit.get(), true);
                    } else {
                        swapTracker.tick(singleton, info, -1, inDragHitbox(singleton, info, pos));
                        int cooldown = singleton.handleHitboxInteract(info, Minecraft.getInstance().player, List.of(hit.get()), InteractionHand.values()[c], Minecraft.getInstance().options.keyAttack.isDown());
                        if (singleton.isVROnly()) {
                            cooldown = (int) (cooldown / 1.5);
                        }
                        setCooldown(cooldown);
                    }
                    return true;
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

    protected static boolean handleInfo(AbstractPlayerAttachmentImmersive<?, ?> singleton, AbstractPlayerAttachmentInfo info, IVRPlayer vrPlayer) {
        if (info.hasHitboxes() && singleton.hitboxesAvailable(info)) {
            for (int c = 0; c <= 1; c++) {
                IVRData controller = vrPlayer.getController(c);
                Vec3 pos = controller.position();
                Optional<Integer> hit = Util.getFirstIntersect(pos, info.getAllHitboxes());
                if (hit.isPresent()) {
                    singleton.onAnyRightClick(info);
                    singleton.handleRightClick(info, Minecraft.getInstance().player, hit.get(),
                            c == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
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

    private static <I extends ImmersiveInfo> boolean inDragHitbox(Immersive<I, ?> singleton, I info, Vec3 pos) {
        BoundingBox dragHitbox = singleton.getDragHitbox(info);
        return dragHitbox != null && Util.getFirstIntersect(pos, dragHitbox).isPresent();
    }
}
