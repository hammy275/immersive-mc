package net.blf02.immersivemc.client;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.config.PlacementMode;
import net.blf02.immersivemc.common.vr.VRPlugin;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientUtil {


    public static int immersiveLeftClickCooldown = 0;

    @OnlyIn(Dist.CLIENT)
    public static Tuple<Vector3d, Vector3d> getStartAndEndOfLookTrace(PlayerEntity player) {
        double dist = Minecraft.getInstance().gameMode.getPickRange();
        Vector3d start;
        Vector3d end;
        if (VRPluginVerify.clientInVR) {
            start = VRPlugin.API.getVRPlayer(player).getController0().position();
            Vector3d viewVec = VRPlugin.API.getVRPlayer(player).getController0().getLookAngle();
            end = start.add(viewVec.x * dist, viewVec.y * dist, viewVec.z * dist);
        } else {
            start = player.getEyePosition(1);
            Vector3d viewVec = player.getViewVector(1);
            end = player.getEyePosition(1).add(viewVec.x * dist, viewVec.y * dist,
                    viewVec.z * dist);
        }
        return new Tuple<>(start, end);
    }

    @OnlyIn(Dist.CLIENT)
    public static void setRightClickCooldown(int amount) {
        Minecraft.getInstance().rightClickDelay = amount;
    }

    public static PlacementMode getPlacementModeIndirect() {
        return Minecraft.getInstance().options.keyAttack.isDown() && VRPluginVerify.clientInVR
                ? PlacementMode.PLACE_ALL : ActiveConfig.placementMode;
    }
}
