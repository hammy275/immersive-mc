package com.hammy275.immersivemc.client.ticker;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.GrabItemPacket;
import com.hammy275.immersivemc.common.ticker.AbstractTicker;
import com.hammy275.immersivemc.common.util.RGBA;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class RangedGrabTickerClient extends AbstractTicker {

    public static final double threshold = 0.1;

    protected ItemEntity selected = null;

    @Override
    protected void tick(Player player, VRPose pose, VRPoseHistory poseHistory) {
        VRBodyPartData controller = pose.getMainHand();

        double dist = ActiveConfig.active().rangedGrabRange == -1 ?
                player.blockInteractionRange() : ActiveConfig.active().rangedGrabRange;
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
                            new Vector3f(color.redF(), color.greenF(), color.blueF()), color.alphaF()),
                    pos.x, pos.y, pos.z, 0.01, 0.01, 0.01);
        }
    }

    @Override
    protected boolean shouldTick(Player player, VRPose pose, VRPoseHistory poseHistory) {
        return Minecraft.getInstance().gameMode != null && ActiveConfig.active().useRangedGrabImmersive;
    }
}
