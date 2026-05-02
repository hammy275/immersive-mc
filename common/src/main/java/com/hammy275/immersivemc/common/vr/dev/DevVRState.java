package com.hammy275.immersivemc.common.vr.dev;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.PlatformClient;
import com.hammy275.immersivemc.common.vr.dev.impl.DevVRBodyPartyData;
import com.hammy275.immersivemc.common.vr.dev.impl.DevVRPose;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.math.Vector3f;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.api.client.HeldInteractModule;
import org.vivecraft.api.client.InteractModule;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.common.api_impl.data.VRPoseHistoryImpl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * A class that holds data and handles tick logic to replicate VR functionality outside VR. Unlike Vivecraft's NullVR
 * VR implementation, which focuses on creating a more full VR implementation for debugging Vivecraft, this focuses
 * much more on debugging ImmersiveMC. The ability to change VR data is limited (no FBT, etc.), but the much faster
 * movement makes it possible to mostly test ImmersiveMC features outside VR.
 * <p>
 * This is effectively the spiritual successor to MC VR API's dev mode.
 */
public class DevVRState {

    public static boolean inVR = true;
    public static DevVRPose pose = new DevVRPose(
            new DevVRBodyPartyData(Vec3.ZERO, new Vec3(1, 0, 0)),
            new DevVRBodyPartyData(Vec3.ZERO, new Vec3(1, 0, 0)),
            new DevVRBodyPartyData(Vec3.ZERO, new Vec3(1, 0, 0))
    );
    public static final VRPoseHistoryImpl poseHistory = new VRPoseHistoryImpl();
    public static final List<Tracker> trackers = new ArrayList<>();
    public static final List<InteractModule> interactModules = new ArrayList<>();

    public static final EnumMap<InteractionHand, InteractModule> activeModules = new EnumMap<>(InteractionHand.class);

    private static KeyMapping toggleVR;
    private static KeyMapping placeHMD;
    private static KeyMapping placeC0;
    private static KeyMapping placeC1;

    public static void clientInit() {
        // Keybindings below intentionally don't have translations so people translating don't have to translate these.
        // As it turns out, translation keys here can be strings written like this, so I'll just use that and let the
        // lack of translation not matter.
        toggleVR = new KeyMapping("ImmersiveMC Dev VR: Toggle VR",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UP, ImmersiveMC.vrKeyCategory);
        placeHMD = new KeyMapping("ImmersiveMC Dev VR: Place HMD",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, ImmersiveMC.vrKeyCategory);
        placeC0 = new KeyMapping("ImmersiveMC Dev VR: Place Main Hand",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, ImmersiveMC.vrKeyCategory);
        placeC1 = new KeyMapping("ImmersiveMC Dev VR: Place Off Hand",
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, ImmersiveMC.vrKeyCategory);
        PlatformClient.registerKeyMapping(toggleVR);
        PlatformClient.registerKeyMapping(placeHMD);
        PlatformClient.registerKeyMapping(placeC0);
        PlatformClient.registerKeyMapping(placeC1);
    }

    public static void clientTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && !Minecraft.getInstance().isPaused()) {
            if (toggleVR.consumeClick()) {
                inVR = !inVR;
                if (inVR) {
                    pose = new DevVRPose(
                            new DevVRBodyPartyData(Vec3.ZERO, new Vec3(1, 0, 0)),
                            new DevVRBodyPartyData(Vec3.ZERO, new Vec3(1, 0, 0)),
                            new DevVRBodyPartyData(Vec3.ZERO, new Vec3(1, 0, 0))
                    );
                    player.displayClientMessage(new TextComponent("Toggled VR on!"), false);
                } else {
                    pose = null;
                    poseHistory.clear();
                    player.displayClientMessage(new TextComponent("Toggled VR off!"), false);
                }
            }

            if (!inVR) {
                return;
            }

            if (placeHMD.isDown()) {
                pose = new DevVRPose(
                        new DevVRBodyPartyData(player.getEyePosition().add(player.getLookAngle().scale(2)), player.getLookAngle()),
                        pose.c0(),
                        pose.c1()
                );
            }
            if (placeC0.isDown()) {
                pose = new DevVRPose(
                        pose.head(),
                        new DevVRBodyPartyData(player.getEyePosition().add(player.getLookAngle().scale(2)), player.getLookAngle()),
                        pose.c1()
                );
            }
            if (placeC1.isDown()) {
                pose = new DevVRPose(
                        pose.head(),
                        pose.c0(),
                        new DevVRBodyPartyData(player.getEyePosition().add(player.getLookAngle().scale(2)), player.getLookAngle())
                );
            }

            player.level.addParticle(new DustParticleOptions(new Vector3f(1, 1, 1), 1f),
                    pose.head().pos().x, pose.head().pos().y, pose.head().pos().z,
                    0, 0, 0);
            player.level.addParticle(new DustParticleOptions(new Vector3f(0, 0, 0), 0.5f),
                    pose.head().pos().x + pose.head().rot().x,
                    pose.head().pos().y  + pose.head().rot().y,
                    pose.head().pos().z +  + pose.head().rot().z,
                    0, 0, 0);

            player.level.addParticle(new DustParticleOptions(new Vector3f(0, 0, 1), 1f),
                    pose.c0().pos().x, pose.c0().pos().y, pose.c0().pos().z,
                    0, 0, 0);
            player.level.addParticle(new DustParticleOptions(new Vector3f(0, 0, 0), 0.5f),
                    pose.c0().pos().x + pose.c0().rot().x,
                    pose.c0().pos().y  + pose.c0().rot().y,
                    pose.c0().pos().z +  + pose.c0().rot().z,
                    0, 0, 0);

            player.level.addParticle(new DustParticleOptions(new Vector3f(1, 0, 0), 1f),
                    pose.c1().pos().x, pose.c1().pos().y, pose.c1().pos().z,
                    0, 0, 0);
            player.level.addParticle(new DustParticleOptions(new Vector3f(0, 0, 0), 0.5f),
                    pose.c1().pos().x + pose.c1().rot().x,
                    pose.c1().pos().y  + pose.c1().rot().y,
                    pose.c1().pos().z +  + pose.c1().rot().z,
                    0, 0, 0);

            poseHistory.addPose(pose, Minecraft.getInstance().player.position());

            for (Tracker tracker : trackers) {
                tracker.idleProcess(player);
                if (tracker.isActive(player)) {
                    tracker.activeProcess(player);
                } else {
                    tracker.inactiveProcess(player);
                }
            }

            boolean leftDown = Minecraft.getInstance().options.keyAttack.isDown();
            for (InteractionHand hand : InteractionHand.values()) {
                InteractModule active = activeModules.get(hand);
                if (active != null) {
                    if (leftDown && active instanceof HeldInteractModule held && held.onHoldTick(player, hand)) {
                        continue;
                    }
                    if (active instanceof HeldInteractModule held) {
                        held.onRelease(player, hand);
                    }
                    active.reset(player, hand);
                    activeModules.remove(hand);
                }
                for (InteractModule module : interactModules) {
                    if (leftDown && module.isActive(player, hand, pose.getHand(hand).getPos())) {
                        module.onPress(player, hand);
                        activeModules.put(hand, module);
                        break;
                    }
                }
            }
        }
    }

}
