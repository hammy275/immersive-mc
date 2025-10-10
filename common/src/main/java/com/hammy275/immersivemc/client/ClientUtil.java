package com.hammy275.immersivemc.client;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveMCClientRegistration;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.client.config.screen.ConfigScreen;
import com.hammy275.immersivemc.client.immersive.AbstractPlayerAttachmentImmersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClientUtil {


    public static final int maxLight = LightTexture.pack(15, 15);
    public static int immersiveLeftClickCooldown = 0;

    public static RegistryAccess getRegistryAccess() {
        return Minecraft.getInstance().level.registryAccess();
    }

    // Doesn't exist in older Minecraft versions ImmersiveMC supports
    public static Vec3 lerpVec3(Vec3 start, Vec3 end, float partialTick) {
        return new Vec3(
                Mth.lerp(partialTick, start.x, end.x),
                Mth.lerp(partialTick, start.y, end.y),
                Mth.lerp(partialTick, start.z, end.z)
        );
    }

    /**
     * Get ray trace start and end for VR.
     * @param device Device. -1 for HMD, 0 for c0, 1 for c1.
     * @return Pair containing start and end positions.
     */
    public static Pair<Vec3, Vec3> getVRStartAndEnd(int device) {
        VRPose vrPose = VRClientAPI.instance().getPreTickWorldPose();
        VRBodyPartData vrData = device == -1 ? vrPose.getHead() : vrPose.getHand(InteractionHand.values()[device]);
        double dist = Minecraft.getInstance().player.blockInteractionRange();
        Vec3 start = vrData.getPos();
        Vec3 look = vrData.getDir();
        Vec3 end = start.add(look.x * dist, look.y * dist, look.z * dist);
        return new Pair<>(start, end);
    }

    public static void clearDisabledImmersives() {
        for (Immersive<?, ?> immersive : Immersives.IMMERSIVES) {
            if (!immersive.getHandler().enabledInConfig(Minecraft.getInstance().player)) {
                immersive.getTrackedObjects().clear();
            }
        }
        for (AbstractPlayerAttachmentImmersive<?, ?> immersive : Immersives.IMMERSIVE_ATTACHMENTS) {
            if (!immersive.enabledInConfig()) {
                immersive.clearImmersives();
            }
        }
    }

    public static ImmersiveConfigScreenInfo createConfigScreenInfo(String keyName, Supplier<ItemStack> optionItem,
                                                                   Function<ActiveConfig, Boolean> configGetter,
                                                                   BiConsumer<ActiveConfig, Boolean> configSetter) {
        return ImmersiveMCClientRegistration.instance().createConfigScreenInfoOneItem(ImmersiveMC.MOD_ID,
                "config." + ImmersiveMC.MOD_ID + "." + keyName,
                optionItem, Component.translatable("config." + ImmersiveMC.MOD_ID + "." + keyName + ".desc"),
                () -> configGetter.apply(ConfigScreen.getAdjustingConfig()),
                (newVal) -> configSetter.accept(ConfigScreen.getAdjustingConfig(), newVal));
    }

    /**
     * Gets player position while accounting for the partial tick.
     * @return The interpolated player position.
     */
    public static Vec3 playerPos() {
        return Minecraft.getInstance().player.getPosition(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true));
    }

    public static Tuple<Vec3, Vec3> getStartAndEndOfLookTrace(Player player) {
        double dist = Minecraft.getInstance().player.blockInteractionRange();
        Vec3 start;
        Vec3 viewVec;
        Vec3 end;
        if (VRPluginVerify.clientInVR()) {
            start = VRAPI.instance().getVRPose(player).getMainHand().getPos();
            viewVec = VRAPI.instance().getVRPose(player).getMainHand().getDir();
        } else {
            start = player.getEyePosition(1);
            viewVec = player.getViewVector(1);
        }
        if (Minecraft.getInstance().hitResult instanceof BlockHitResult bhr && bhr.getType() == HitResult.Type.BLOCK) {
            dist = bhr.getLocation().distanceTo(start);
        }
        end = start.add(viewVec.x * dist, viewVec.y * dist, viewVec.z * dist);
        return new Tuple<>(start, end);
    }

    /**
     * Get Direction best represented by the velocity of a controller
     * @param velocity Velocity from getVelocity()
     * @return A Direction best representing the current direction of the velocity.
     */
    public static Direction getClosestDirection(Vec3 velocity) {
        double max = Math.max(Math.abs(velocity.x), Math.max(Math.abs(velocity.y), Math.abs(velocity.z)));
        if (max == Math.abs(velocity.x)) {
            return velocity.x < 0 ? Direction.WEST : Direction.EAST;
        } else if (max == Math.abs(velocity.y)) {
            return velocity.y < 0 ? Direction.DOWN : Direction.UP;
        } else {
            return velocity.z < 0 ? Direction.NORTH : Direction.SOUTH;
        }
    }

    public static void openBag(Player player) {
        if (VRPluginVerify.hasAPI) {
            if (VRAPI.instance().isVRPlayer(player)) {
                Immersives.immersiveBackpack.doTrack();
            } else {
                player.displayClientMessage(Component.translatable("message.immersivemc.not_in_vr"), false);
            }
        } else {
            player.displayClientMessage(Component.translatable("message.immersivemc.no_api",
                    CommonConstants.vrAPIVersionAsString(), CommonConstants.firstNonCompatibleFutureVersionAsString()), false);
        }
    }

    @Nullable
    public static <I extends ImmersiveInfo> I findImmersive(Immersive<I, ?> immersive, BlockPos pos) {
        for (I info : immersive.getTrackedObjects()) {
            if (Util.getValidBlocks(immersive.getHandler(), info.getBlockPosition(), Minecraft.getInstance().level).contains(pos)) {
                return info;
            }
        }
        return null;
    }
}
