package com.hammy275.immersivemc.client;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveMCClientRegistration;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.client.immersive.BlockBasedImmersive;
import com.hammy275.immersivemc.api.client.immersive.BlockBasedImmersiveInfo;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.common.ImmersiveLogicHelpers;
import com.hammy275.immersivemc.client.config.screen.ConfigScreen;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.info.BagInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VR;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.gizmos.Gizmo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClientUtil {


    public static final int maxLight = LightCoordsUtil.pack(15, 15);
    public static int immersiveLeftClickCooldown = 0;

    public static boolean isVROnly(Immersive<?, ?, ?> immersive) {
        if (immersive instanceof BlockBasedImmersive<?, ?, ?> blockBased) {
            return blockBased.isVROnly();
        } else {
            return true;
        }
    }

    /**
     * Renders a gizmo immediately. Should NOT add instances via {@link net.minecraft.gizmos.Gizmos}, since this renders
     * them directly instead.
     * @param gizmo Gizmo to render
     * @param poseStack PoseStack for rendering.
     */
    public static void renderGizmo(Gizmo gizmo, PoseStack poseStack) {
        DrawableGizmoPrimitives gizmoPrimitives = new DrawableGizmoPrimitives();
        gizmo.emit(gizmoPrimitives, 1f);
        renderGizmoPrimitives(gizmoPrimitives, poseStack);
    }

    /**
     * Renders gizmo primitives immediately. Should NOT add instances via {@link net.minecraft.gizmos.Gizmos}, since
     * this renders them directly instead.
     * @param gizmoPrimitives Gizmo primitives to render
     * @param poseStack PoseStack for rendering.
     */
    public static void renderGizmoPrimitives(DrawableGizmoPrimitives gizmoPrimitives, PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        CameraRenderState cameraRenderState = mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
        // Frustum found via basically the same code as GameRenderer#renderLevel() in the one line below
        Matrix4f frustum = new Matrix4f().rotation(
                mc.gameRenderer.mainCamera().rotation().conjugate(new Quaternionf())
        );
        gizmoPrimitives.submit(ImmersiveRenderHelpers.instance().nodeStorage(), cameraRenderState, false);
    }

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
        VRPose vrPose = VR.ClientAPI.getPreTickWorldPose();
        VRBodyPartData vrData = device == -1 ? vrPose.getHead() : vrPose.getHand(InteractionHand.values()[device]);
        double dist = Minecraft.getInstance().player.blockInteractionRange();
        Vec3 start = vrData.getPos();
        Vec3 look = vrData.getDir();
        Vec3 end = start.add(look.x * dist, look.y * dist, look.z * dist);
        return new Pair<>(start, end);
    }

    public static void clearDisabledImmersives() {
        for (Immersive<?, ?, ?> immersive : Immersives.ALL_IMMERSIVES) {
            if (!immersive.getHandler().enabledInConfig(Minecraft.getInstance().player)) {
                immersive.getTrackedObjects().clear();
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

    public static Pair<Vec3, Vec3> getStartAndEndOfLookTrace(Player player) {
        double dist = Minecraft.getInstance().player.blockInteractionRange();
        Vec3 start;
        Vec3 viewVec;
        Vec3 end;
        if (VRVerify.clientInVR()) {
            start = VR.API.getVRPose(player).getMainHand().getPos();
            viewVec = VR.API.getVRPose(player).getMainHand().getDir();
        } else {
            start = player.getEyePosition(1);
            viewVec = player.getViewVector(1);
        }
        if (Minecraft.getInstance().hitResult instanceof BlockHitResult bhr && bhr.getType() == HitResult.Type.BLOCK) {
            dist = bhr.getLocation().distanceTo(start);
        }
        end = start.add(viewVec.x * dist, viewVec.y * dist, viewVec.z * dist);
        return new Pair<>(start, end);
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

    public static void openBag(Player player, boolean doRumble) {
        if (ActiveConfig.active().useBagImmersive) {
            if (VRVerify.hasAPI) {
                if (VR.API.isVRPlayer(player)) {
                    if (doRumble) {
                        VRRumble.rumbleIfVR(Minecraft.getInstance().player, ActiveConfig.active().swapBagHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, CommonConstants.vibrationTimePlayerActionAlert);
                    }
                    BagInfo alreadyOpenBagInfo = Immersives.immersiveBag.getLocalPlayerInfo();
                    if (alreadyOpenBagInfo == null) {
                        ImmersiveLogicHelpers.instance().startTrackingOnServer(ImmersiveHandlers.bagHandler, Minecraft.getInstance().player);
                    } else {
                        ImmersiveLogicHelpers.instance().stopTrackingOnServer(ImmersiveHandlers.bagHandler, Minecraft.getInstance().player);
                    }
                } else {
                    player.sendSystemMessage(Component.translatable("message.immersivemc.not_in_vr"));
                }
            } else {
                player.sendSystemMessage(Component.translatable("message.immersivemc.no_vivecraft",
                        CommonConstants.vrAPIVersionAsString(), CommonConstants.firstNonCompatibleFutureVersionAsString()));
            }
        }
    }

    @Nullable
    public static <I extends BlockBasedImmersiveInfo> I findImmersive(BlockBasedImmersive<I, ?, ?> immersive, BlockPos pos) {
        for (I info : immersive.getTrackedObjects()) {
            if (Util.getValidBlocks(immersive.getHandler(), info.getBlockPosition(), Minecraft.getInstance().level).contains(pos)) {
                return info;
            }
        }
        return null;
    }
}
