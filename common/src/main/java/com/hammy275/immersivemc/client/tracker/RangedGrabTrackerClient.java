package com.hammy275.immersivemc.client.tracker;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.GrabItemPacket;
import com.hammy275.immersivemc.common.tracker.AbstractTracker;
import com.hammy275.immersivemc.common.util.RGBA;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class RangedGrabTrackerClient extends AbstractTracker {

    public static final double threshold = 0.1;

    protected IVRData last = null;
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
        IVRPlayer vrPlayer = VRPlugin.API.getVRPlayer(player);
        IVRData controller = vrPlayer.getController0();

        if (cooldown <= 0) {
            double dist = ActiveConfig.active().rangedGrabRange == -1 ?
                    Minecraft.getInstance().gameMode.getPickRange() : ActiveConfig.active().rangedGrabRange;

            if (last != null) {
                if (Minecraft.getInstance().options.keyAttack.isDown() ||
                        ImmersiveMC.RANGED_GRAB_KEY.isDown()) {
                    boolean grabFromMove = controller.position().y - last.position().y > threshold && Minecraft.getInstance().options.keyAttack.isDown();
                    boolean grabFromKey = ImmersiveMC.RANGED_GRAB_KEY.isDown();
                    if ((grabFromKey || grabFromMove) && selected != null) {
                        Network.INSTANCE.sendToServer(new GrabItemPacket(selected));
                        selected = null;
                    }
                } else {
                    selected = null;

                    Vec3 start = controller.position();
                    Vec3 viewVec = controller.getLookAngle();
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
                RGBA color = ActiveConfig.active().rangedGrabColor;
                Vec3 pos = selected.position().add(0, 0.2, 0);
                selected.level().addParticle(new DustParticleOptions(
                        new Vector3f(color.redF(), color.greenF(), color.blueF()), color.alphaF()),
                        pos.x, pos.y, pos.z, 0.01, 0.01, 0.01);
            }
        }

        last = controller;
    }

    @Override
    protected boolean shouldTick(Player player) {
        return VRPluginVerify.clientInVR() && Minecraft.getInstance().gameMode != null
                && ActiveConfig.active().useRangedGrab && VRPlugin.API.apiActive(player);
    }
}
