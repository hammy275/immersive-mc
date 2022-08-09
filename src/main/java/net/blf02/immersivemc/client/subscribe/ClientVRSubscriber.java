package net.blf02.immersivemc.client.subscribe;

import net.blf02.immersivemc.client.immersive.AbstractImmersive;
import net.blf02.immersivemc.client.immersive.Immersives;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.common.util.Util;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.blf02.vrapi.event.VRPlayerTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.Optional;

public class ClientVRSubscriber {

    // Global cooldown to prevent rapid-fire VR interactions
    protected int cooldown = 0;

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void immersiveTickVR(VRPlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.side != LogicalSide.CLIENT) return;
        if (Minecraft.getInstance().gameMode == null) return;
        VRPluginVerify.clientInVR = true;

        // Track things the HMD is looking at (cursor is already covered in ClientLogicSubscriber)
        double dist = Minecraft.getInstance().gameMode.getPickRange();
        Vec3 start = event.vrPlayer.getHMD().position();
        Vec3 look = event.vrPlayer.getHMD().getLookAngle();
        Vec3 end = event.vrPlayer.getHMD().position().add(look.x * dist, look.y * dist, look.z * dist);
        BlockHitResult res = event.player.level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE,
                null));
        ClientLogicSubscriber.possiblyTrack(res.getBlockPos(), event.player.level.getBlockState(res.getBlockPos()),
                event.player.level.getBlockEntity(res.getBlockPos()), Minecraft.getInstance().level);

        if (cooldown > 0) {
            cooldown--;
        } else {
            for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
                for (AbstractImmersiveInfo info : singleton.getTrackedObjects()) {
                    if (handleInfo(singleton, info, event.vrPlayer)) {
                        return;
                    }
                }
            }
        }
    }

    protected boolean handleInfo(AbstractImmersive singleton, AbstractImmersiveInfo info, IVRPlayer vrPlayer) {
        if (info.hasHitboxes()) {
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
