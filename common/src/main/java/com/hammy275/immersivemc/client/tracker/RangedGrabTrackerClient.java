package com.hammy275.immersivemc.client.tracker;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.GrabItemPacket;
import com.hammy275.immersivemc.common.tracker.AbstractTracker;
import com.hammy275.immersivemc.common.util.RGBA;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRVerify;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class RangedGrabTrackerClient extends AbstractTracker {

    public static final double threshold = 0.1;

    protected ItemEntity selected = null;
    protected int cooldown = 0;

    public RangedGrabTrackerClient() {
        ClientTrackerInit.trackers.add(this);
    }

    @Override
    protected void tick(Player player) {
        if (cooldown > 0) {
            cooldown--;
        }
        VRPose vrPose = VRAPI.instance().getVRPose(player);
        VRBodyPartData controller = vrPose.getMainHand();

        if (cooldown <= 0) {
            double dist = ActiveConfig.active().rangedGrabRange == -1 ?
                    Minecraft.getInstance().player.blockInteractionRange() : ActiveConfig.active().rangedGrabRange;
            VRPoseHistory history = VRAPI.instance().getHistoricalVRPoses(player);
            if (history.ticksOfHistory() >= 1) {
                VRBodyPartData last = history.getHistoricalData(1).getMainHand();
                if (Minecraft.getInstance().options.keyAttack.isDown() ||
                        ImmersiveMC.RANGED_GRAB_KEY.isDown()) {
                    boolean grabFromMove = controller.getPos().y - last.getPos().y > threshold && Minecraft.getInstance().options.keyAttack.isDown();
                    boolean grabFromKey = ImmersiveMC.RANGED_GRAB_KEY.isDown();
                    if ((grabFromKey || grabFromMove) && selected != null) {
                        Network.INSTANCE.sendToServer(new GrabItemPacket(selected));
                        selected = null;
                    }
                } else {
                    selected = null;

                    Vec3 start = controller.getPos();
                    Vec3 viewVec = controller.getDir();
                    Vec3 end = start.add(viewVec.x * dist, viewVec.y * dist,
                            viewVec.z * dist);

                    List<Entity> ents = player.level().getEntities(player, player.getBoundingBox().inflate(10),
                            (entity -> entity instanceof ItemEntity && Util.canPickUpItem((ItemEntity) entity, player)));
                    List<AABB> hitboxes = new LinkedList<>();
                    for (Entity ent : ents) {
                        hitboxes.add(ent.getBoundingBox().inflate(1d/3d));
                    }
                    Optional<Integer> result = Util.rayTraceClosest(start, end, hitboxes.toArray(new AABB[0]));

                    if (result.isPresent()) {
                        selected = (ItemEntity) ents.get(result.get());
                    }
                }
            }

            if (selected != null) {
                RGBA color = ClientRenderSubscriber.rangedGrabColor();
                Vec3 pos = selected.position().add(0, 0.2, 0);
                selected.level().addParticle(new DustParticleOptions(
                                (int) color.toLong() & 0xFFFFFF, color.alphaF()),
                        pos.x, pos.y, pos.z, 0.01, 0.01, 0.01);
            }
        }
    }

    @Override
    protected boolean shouldTick(Player player) {
        return VRVerify.clientInVR() && Minecraft.getInstance().gameMode != null
                && ActiveConfig.active().useRangedGrabImmersive;
    }
}
