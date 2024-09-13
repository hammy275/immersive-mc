package com.hammy275.immersivemc.client.subscribe;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.AbstractPlayerAttachmentImmersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
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

import java.util.Optional;

public class ClientVRSubscriber {

    // Global cooldown to prevent rapid-fire VR interactions
    protected static int cooldown = 0;

    public static void setCooldown(int cooldown) {
        ClientVRSubscriber.cooldown = Math.max(ClientVRSubscriber.cooldown, cooldown);
    }

    public static void immersiveTickVR(Player player) {
        if (!Platform.isClient()) return;
        if (Minecraft.getInstance().gameMode == null) return;
        if (!VRPlugin.API.playerInVR(player)) return;
        IVRPlayer vrPlayer = VRPlugin.API.getVRPlayer(player);

        // Track things the HMD is looking at (cursor is already covered in ClientLogicSubscriber)
        double dist = Minecraft.getInstance().gameMode.getPickRange();
        Vec3 start = vrPlayer.getHMD().position();
        Vec3 look = vrPlayer.getHMD().getLookAngle();
        Vec3 end = vrPlayer.getHMD().position().add(look.x * dist, look.y * dist, look.z * dist);
        BlockHitResult res = player.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE,
                player));
        ClientLogicSubscriber.possiblyTrack(res.getBlockPos(), player.level().getBlockState(res.getBlockPos()),
                player.level().getBlockEntity(res.getBlockPos()), Minecraft.getInstance().level);

        if (cooldown > 0) {
            cooldown--;
        } else {
            for (Immersive<?, ?> singleton : Immersives.IMMERSIVES) {
                if (handleInfos(singleton, vrPlayer)) {
                    return;
                }
            }
            for (AbstractPlayerAttachmentImmersive<? extends AbstractPlayerAttachmentInfo, ?> singleton : Immersives.IMMERSIVE_ATTACHMENTS) {
                for (AbstractPlayerAttachmentInfo info : singleton.getTrackedObjects()) {
                    if (handleInfo(singleton, info, vrPlayer)) {
                        return;
                    }
                }
            }
        }
    }

    protected static <I extends ImmersiveInfo> boolean handleInfos(Immersive<I, ?> singleton, IVRPlayer vrPlayer) {
        for (I info : singleton.getTrackedObjects()) {
            if (info.hasHitboxes()) {
                for (int c = 0; c <= 1; c++) {
                    IVRData controller = vrPlayer.getController(c);
                    Vec3 pos = controller.position();
                    Optional<Integer> hit = Util.getFirstIntersect(pos, info.getAllHitboxes());
                    if (hit.isPresent() &&
                            (Minecraft.getInstance().options.keyAttack.isDown() || !info.getAllHitboxes().get(hit.get()).isTriggerHitbox())) {
                        int cooldownFromInfo = singleton.handleHitboxInteract(info, Minecraft.getInstance().player, hit.get(),
                                c == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
                        if (cooldownFromInfo >= 0) {
                            if (Minecraft.getInstance().options.keyAttack.isDown()) {
                                // Set longer cooldown if whole stack is placed
                                cooldown = (int) (cooldownFromInfo * (singleton.isVROnly() ? 1.5 : ClientConstants.cooldownVRMultiplier + 0.5));
                            } else {
                                cooldown = (int) (cooldownFromInfo * (singleton.isVROnly() ? 1 : ClientConstants.cooldownVRMultiplier));
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
}
