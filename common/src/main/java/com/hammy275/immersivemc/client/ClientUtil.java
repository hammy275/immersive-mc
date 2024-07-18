package com.hammy275.immersivemc.client;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveMCClientRegistration;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.mixin.MinecraftMixinAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ClientUtil {


    public static final int maxLight = LightTexture.pack(15, 15);
    public static int immersiveLeftClickCooldown = 0;

    public static ImmersiveConfigScreenInfo createConfigScreenInfo(String keyName, Supplier<ItemStack> optionItem,
                                                                   ForgeConfigSpec.BooleanValue configEntry) {
        return ImmersiveMCClientRegistration.instance().createConfigScreenInfoOneItem(ImmersiveMC.MOD_ID,
                "config." + ImmersiveMC.MOD_ID + "." + keyName,
                optionItem, Component.translatable("config." + ImmersiveMC.MOD_ID + "." + keyName + ".desc"),
                configEntry, (newVal) -> {
                    configEntry.set(newVal);
                    ActiveConfig.FILE.loadFromFile();
                });
    }

    /**
     * Gets player position while accounting for partial ticks (getFrameTime())
     * @return Player position while accounting for partial ticks
     */
    public static Vec3 playerPos() {
        return Minecraft.getInstance().player.getPosition(Minecraft.getInstance().getFrameTime());
    }

    public static Tuple<Vec3, Vec3> getStartAndEndOfLookTrace(Player player) {
        double dist = Minecraft.getInstance().gameMode.getPickRange();
        Vec3 start;
        Vec3 viewVec;
        Vec3 end;
        if (VRPluginVerify.clientInVR()) {
            start = VRPlugin.API.getVRPlayer(player).getController0().position();
            viewVec = VRPlugin.API.getVRPlayer(player).getController0().getLookAngle();
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

    public static void setRightClickCooldown(int amount) {
        ((MinecraftMixinAccessor) Minecraft.getInstance()).setRightClickDelay(amount);
    }

    public static PlacementMode getPlacementModeIndirect() {
        return getPlacementModeIndirect(false);
    }

    public static PlacementMode getPlacementModeIndirect(boolean leftClickAlreadyDoesSomething) {
        return Minecraft.getInstance().options.keyAttack.isDown() &&
                !leftClickAlreadyDoesSomething ? PlacementMode.PLACE_ALL : ActiveConfig.active().placementMode;
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
            if (VRPlugin.API.playerInVR(player)) {
                if (VRPlugin.API.apiActive(player)) {
                    Immersives.immersiveBackpack.doTrack();
                } else {
                    player.sendSystemMessage(Component.translatable("message.immersivemc.no_api_server"));
                }
            } else {
                player.sendSystemMessage(Component.translatable("message.immersivemc.not_in_vr"));
            }
        } else {
            player.sendSystemMessage(Component.translatable("message.immersivemc.no_api",
                    CommonConstants.vrAPIVersionAsString(), CommonConstants.firstNonCompatibleFutureVersionAsString()));
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
