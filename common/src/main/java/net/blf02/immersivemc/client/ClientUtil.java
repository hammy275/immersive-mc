package net.blf02.immersivemc.client;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.config.PlacementMode;
import net.blf02.immersivemc.common.vr.VRPlugin;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.blf02.immersivemc.mixin.MinecraftMixinAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ClientUtil {


    public static int immersiveLeftClickCooldown = 0;

    public static Tuple<Vec3, Vec3> getStartAndEndOfLookTrace(Player player) {
        double dist = Minecraft.getInstance().gameMode.getPickRange();
        Vec3 start;
        Vec3 end;
        if (VRPluginVerify.clientInVR) {
            start = VRPlugin.API.getVRPlayer(player).getController0().position();
            Vec3 viewVec = VRPlugin.API.getVRPlayer(player).getController0().getLookAngle();
            end = start.add(viewVec.x * dist, viewVec.y * dist, viewVec.z * dist);
        } else {
            start = player.getEyePosition(1);
            Vec3 viewVec = player.getViewVector(1);
            end = player.getEyePosition(1).add(viewVec.x * dist, viewVec.y * dist,
                    viewVec.z * dist);
        }
        return new Tuple<>(start, end);
    }

    public static void setRightClickCooldown(int amount) {
        ((MinecraftMixinAccessor) Minecraft.getInstance()).setRightClickDelay(amount);
    }

    public static PlacementMode getPlacementModeIndirect() {
        return getPlacementModeIndirect(false);
    }

    public static PlacementMode getPlacementModeIndirect(boolean leftClickAlreadyDoesSomething) {
        return Minecraft.getInstance().options.keyAttack.isDown() &&
                !leftClickAlreadyDoesSomething ? PlacementMode.PLACE_ALL : ActiveConfig.placementMode;
    }
}
